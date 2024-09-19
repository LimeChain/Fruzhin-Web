package com.limechain.rpc;

import com.limechain.constants.RpcConstants;
import lombok.SneakyThrows;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSString;
import org.teavm.jso.websocket.WebSocket;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * The implementation of {@link WsRpcClient}. Uses a native JS Websocket implementation.
 */
public class WsRpcClientImpl implements WsRpcClient {

    private WebSocket ws;
    private final Queue<String> responseQueue;

    public WsRpcClientImpl() {
        responseQueue = new ArrayDeque<>();
        openWebsocketConnection();
    }

    private void openWebsocketConnection() {
        System.out.println("Initializing RPC websocket connection...");
        //TODO change when configuring chain.
        ws = new WebSocket(RpcConstants.POLKADOT_WS_RPC);
        initHandlers();
    }

    private void initHandlers() {
        ws.onClose(e -> {
            System.out.println("RPC websocket connection was closed.");
            System.out.println("Retrying connection...");
            Window.setTimeout(this::openWebsocketConnection, 1000);
        });

        ws.onError(e -> {
            System.out.println("There was an error in the RPC websocket connection. Closing connection...");
            ws.close();
        });

        ws.onOpen(e -> System.out.println("Websocket connection is open."));
        ws.onMessage(e -> responseQueue.offer(e.getDataAsString()));
    }

    /**
     * Waits for the current ws connection to be in an opened state then sends an RPC request to the full node.
     */
    @Override
    public void send(JSString rpcString) {
        new Thread(() -> {
            handleSocketState();
            ws.send(rpcString.stringValue());
        }).start();
    }

    /**
     * Handles the state of the websocket when sending a message. If the connection is in a closing (2) or a closed (3)
     * state the client throws an error.
     */
    @SneakyThrows
    private void handleSocketState() {
        var startState = ws.getReadyState();

        while (startState != 1) {
            var currentState = ws.getReadyState();
            if (currentState > 1) {
                throw new Exception("Calling function of a closed websocket is prohibited.");
            }

            startState = currentState;
            Thread.sleep(50);
        }
    }

    /**
     * Polls the first item in the queue and returns it as a string.
     */
    @Override
    public String nextResponse() {
        return responseQueue.poll();
    }
}
