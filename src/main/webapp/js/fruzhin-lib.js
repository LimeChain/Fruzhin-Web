import 'https://unpkg.com/@chainsafe/libp2p-gossipsub@14.0.0/dist/index.min.js';
import 'https://unpkg.com/@chainsafe/libp2p-noise@16.0.0/dist/index.min.js';
import 'https://unpkg.com/@chainsafe/libp2p-yamux@7.0.0/dist/index.min.js';
import 'https://unpkg.com/libp2p@2.0.1/dist/index.min.js';
import 'https://unpkg.com/@libp2p/bootstrap@11.0.0/dist/index.min.js';
import 'https://unpkg.com/@libp2p/identify@3.0.0/dist/index.min.js';
import 'https://unpkg.com/@libp2p/kad-dht@13.0.0/dist/index.min.js';
import 'https://unpkg.com/@libp2p/ping@2.0.0/dist/index.min.js';
import 'https://unpkg.com/@libp2p/websockets@9.0.0/dist/index.min.js';
import 'https://unpkg.com/it-pb-stream@4.0.2/dist/index.min.js';
import 'https://unpkg.com/it-pipe@3.0.1/dist/index.min.js';
import 'https://unpkg.com/@muradsenteca/blake2b@1.0.1/dist/index.min.js'
import 'https://unpkg.com/@muradsenteca/ed25519@1.0.0/dist/index.min.js'

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
        connectionEncrypters: [ChainsafeLibp2PNoise.noise()],
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
    ...Fruzhin,
}
