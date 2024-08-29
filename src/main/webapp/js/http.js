async function asyncHttpRequest(method = 'GET', url, body = null, callback) {
    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: method === 'POST' ? body : undefined
        });

        if (!response.ok) {
            callback(new Error(`Request failed with status: ${response?.status}`), null);
            return;
        }

        const result = await response.text();
        callback(null, result);

    } catch (error) {
        callback(new Error(`Error during sending request: ${error?.message}`), null);
    }
}

function httpRequestSync(method, url, body) {
    var xhr = new XMLHttpRequest();
    xhr.open(method, url, false); // false for synchronous request
    xhr.setRequestHeader('Content-Type', 'application/json');
    if (method === 'POST' && body) {
        xhr.send(body);
    } else {
        xhr.send();
    }
    if (xhr.status === 200) {
        return xhr.responseText;
    } else {
        throw new Error('Request failed with status ' + xhr.status);
    }
}

var isRpcExported = false;

function sendRpcRequest(method, params) {
    if (isRpcExported === false) {
        window.setTimeout(() => sendRpcRequest(method, params), 10);
    } else {
        console.log(rpc.sendRequest(method, params));
    }
}
