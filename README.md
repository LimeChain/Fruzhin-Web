![Fruzhin-Cover-Black](https://github.com/LimeChain/Fruzhin/assets/29047760/8e617c9a-005d-44b7-b2bc-d14cc6860726)

# What is Fruzhin?
Fruzhin is a Java implementation of the Polkadot Host. With the help of [TeaVM](https://teavm.org/) it is now available
as a JS based light client in web environments. It makes use of other full nodes to provide RPC server like behaviour.

It's been funded by
[Polkadot Pioneers Prize](https://polkadot.polkassembly.io/child_bounty/238).
> **Warning**
> Fruzhin is in pre-production state

# Example
## Starting Fruzhin
In order to use Fruzhin in your webpage you have to import the appropriate JS files from UNKPG. Then you can call the
`fruzhin.main()` method with a string array parameter that takes one entry. The parameter can be one of the following:
- Well known chain name (polkadot, westend, kusama)
- A chain spec string

```html
<!DOCTYPE html>
<html>
<head>
    <title>Fruzhin</title>
    <script type="module" src="https://unpkg.com/fruzhin/dist/js/fruzhin-lib.js"></script>
</head>
<body>
<script type="module">
    fruzhin.main(['polkadot']);
</script>
</body>
</html>
```

## Making RPC calls
Once Fruzhin is started an RPC client is exported under a `wsRpc` object. Making a call is easy.

`wsRpc.send('{"jsonrpc":"2.0","id":1,"method":"system_name","params":[]}')`

Then the response can be retrieved by calling `wsRpc.nextResponse()`. 

*NOTE* `wsRpc.nextResponse()` returns `null` if no response is present in the queue.
