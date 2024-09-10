import 'https://unpkg.com/@chainsafe/libp2p-yamux/dist/index.min.js';
import 'https://unpkg.com/@chainsafe/libp2p-noise/dist/index.min.js';
import 'https://unpkg.com/@libp2p/websockets/dist/index.min.js';
import 'https://unpkg.com/@libp2p/kad-dht/dist/index.min.js';
import 'https://unpkg.com/@libp2p/identify/dist/index.min.js';
import 'https://unpkg.com/@libp2p/bootstrap/dist/index.min.js';
import 'https://unpkg.com/@libp2p/ping/dist/index.min.js';
import 'https://unpkg.com/@chainsafe/libp2p-gossipsub/dist/index.min.js';
import 'https://unpkg.com/libp2p/dist/index.min.js';
import 'https://unpkg.com/it-pipe/dist/index.min.js';
import 'https://unpkg.com/it-pb-stream/dist/index.min.js';

import * as Blake2b from './blake2b.js';
import * as ED25519 from './ed25519.js';
import * as HTTP from './http.js';
import * as Fruzhin from './fruzhin.js'

var startLibp2p = async (bootnodes) => {

    let test = {
        addresses: {
            listen: [],
        },
        transports: [
            Libp2PWebsockets.webSockets()
        ],
        streamMuxers: [ChainsafeLibp2PYamux.yamux()],
        connectionEncryption: [ChainsafeLibp2PNoise.noise()],
        peerDiscovery: [
            Libp2PBootstrap.bootstrap({
                list: bootnodes
            })
        ],
        services: {
            identify: Libp2PIdentify.identify(),
            ping: Libp2PPing.ping(),
            dht: Libp2PKadDht.kadDHT({protocol: "/dot/kad"}),
            pubsub: ChainsafeLibp2PGossipsub.gossipsub(),
        }
    };

    window.fruzhin.libp = await Libp2P.createLibp2p(test);
    window.fruzhin.libp.start();
}

window.fruzhin = {
    startLibp2p,
    ED25519,
    HTTP,
    ...Blake2b,
    ...Fruzhin,
}
