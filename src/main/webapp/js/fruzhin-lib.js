import 'https://unpkg.com/@chainsafe/libp2p-yamux@6.0.2/dist/index.min.js';
import 'https://unpkg.com/@chainsafe/libp2p-noise@15.1.2/dist/index.min.js';
import 'https://unpkg.com/@libp2p/websockets@8.2.0/dist/index.min.js';
import 'https://unpkg.com/@libp2p/kad-dht@12.1.5/dist/index.min.js';
import 'https://unpkg.com/@libp2p/identify@2.1.5/dist/index.min.js';
import 'https://unpkg.com/@libp2p/bootstrap@10.1.5/dist/index.min.js';
import 'https://unpkg.com/@libp2p/ping@1.1.6/dist/index.min.js';
import 'https://unpkg.com/@chainsafe/libp2p-gossipsub@13.2.0/dist/index.min.js';
import 'https://unpkg.com/libp2p@1.9.4/dist/index.min.js';
import 'https://unpkg.com/it-pipe@3.0.1/dist/index.min.js';
import 'https://unpkg.com/it-pb-stream@4.0.2/dist/index.min.js';
import 'https://unpkg.com/@muradsenteca/ed25519@1.0.0/dist/index.min.js'
import 'https://unpkg.com/@muradsenteca/blake2b@1.0.1/dist/index.min.js'

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
    HTTP,
    ...Fruzhin,
}
