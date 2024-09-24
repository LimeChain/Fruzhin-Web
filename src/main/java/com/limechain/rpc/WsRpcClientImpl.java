package com.limechain.rpc;

import com.limechain.config.AppBean;
import com.limechain.config.ChainService;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSString;
import org.teavm.jso.websocket.WebSocket;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * The implementation of {@link WsRpcClient}. Uses a native JS Websocket implementation.
 */
@Log
public class WsRpcClientImpl implements WsRpcClient {

    private static final int WS_OPEN_WAIT_MS = 50;

    private WebSocket ws;
    private final Queue<String> responseQueue;

    public WsRpcClientImpl() {
        responseQueue = new ArrayDeque<>();
        openWebsocketConnection();
    }

    private void openWebsocketConnection() {
        log.info("Initializing RPC websocket connection...");
        ws = new WebSocket(AppBean.getBean(ChainService.class).getWsRpcEndpoint());
        initHandlers();
    }

    private void initHandlers() {
        ws.onClose(e -> {
            log.info("RPC websocket connection was closed.");
            log.info("Retrying connection...");
            Window.setTimeout(this::openWebsocketConnection, 1000);
        });

        ws.onError(e -> {
            log.warning("There was an error in the RPC websocket connection. Closing connection...");
            ws.close();
        });

        ws.onOpen(e -> log.info("Websocket connection is open."));
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
        int startState = ws.getReadyState();
        int openedState = WebsocketState.OPEN.getIntValue();

        while (startState != openedState) {
            if (startState > openedState) {
                throw new Exception("Calling function of a closed websocket is prohibited.");
            }

            Thread.sleep(WS_OPEN_WAIT_MS);
            startState = ws.getReadyState();
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
