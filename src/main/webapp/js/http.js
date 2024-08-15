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