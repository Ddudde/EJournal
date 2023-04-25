var CACHE_NAME, urlsToCache, num, cacheWhitelist, prom;

// Name our cache
CACHE_NAME = 'my-pwa-cache-v2';

cacheWhitelist = [];//[CACHE_NAME];

function init() {
    num = 0;
    setInterval(() => {
        console.log('tick', num);
        num++;
    }, 2000);
    prom = fetch("/DipvLom/asset-manifest.json")
        .then(response => response.json())
        .then(assets => {
            urlsToCache = [
                "/DipvLom/",
                "/DipvLom/fav32.png",
                "/DipvLom/fav16.png",
                "/DipvLom/manifest.json",
                "/DipvLom/app.js"
            ];
            Object.getOwnPropertyNames(assets.files).map((key, i, x, val = assets.files[key]) => {
                urlsToCache.push(val);
            });
        })
    // Delete old caches that are not our current one!
    this.addEventListener("activate", activateF);
    // The first time the user starts up the PWA, 'install' is triggered.
    this.addEventListener('install', installF);
    // When the webpage goes to fetch files, we intercept that request and serve up the matching files
    // if we have them
    this.addEventListener('fetch', fetchF);
    this.addEventListener('message', messageF);
}

function messageF(e) {
    // event is an ExtendableMessageEvent object
    console.log(`The client sent me a message: ${e.data}`);

    // e.source.postMessage("Hi client");
}

function activateF(e) {
    e.waitUntil(
        (async () => {
            if(this.registration.navigationPreload)
                return this.registration.navigationPreload.enable();
        })()
        // this.registration.unregister()
        //     .then(bool=>{
        //         if(bool) {
        //             console.log("Service worker unregister is successful");
        //         } else {
        //             console.log("Service worker unregister is failed");
        //         }
        //     })
        // caches.keys()
        // .then(keyList =>
        //     Promise.all(keyList.map(key => {
        //         console.log("dsf12");
        //         if (!cacheWhitelist.includes(key)) {
        //             console.log('Deleting cache: ' + key)
        //             return caches.delete(key);
        //         }
        //     }))
        // )
    );
}

function installF(e) {
    e.waitUntil(caches.open(CACHE_NAME)
        .then(cache => prom.then(e => cache.addAll(urlsToCache)
            .then(r => {
                console.log('cached');
                return r;
            })
            .catch(message => {
                console.log(message)
            })
        ))
    );
}

function fetchF(e) {
    if(e.request.destination == '') return;
    e.respondWith(
        e.preloadResponse.then(responsePre => {
            return responsePre || getCache(e);
        }).catch(message => {
            console.log(message)
            if(e.request.destination == 'document') {
                console.log("setIndexDoc...")
                return caches.match("/DipvLom/")
                    .then(responseCache => responseCache)
            } else {
                return getCache(e);
            }
        })
    );
}

function getCache(e) {
    return caches.match(e.request)
        .then(responseCache => {
            return responseCache || fetch(e.request.url)
                .then(req => console.log("fetch!"))
                .catch(message => console.log(message));
        })
}

init();