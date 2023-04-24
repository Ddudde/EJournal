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
    prom = fetch("./asset-manifest.json")
        .then(response => response.json())
        .then(assets => {
            urlsToCache = [
                "/DipvLom/",
                "/DipvLom/news/",
                "/DipvLom/news/por/",
                "/DipvLom/news/yo/",
                "/DipvLom/contacts/",
                "/DipvLom/contacts/por/",
                "/DipvLom/contacts/yo/",
                "/DipvLom/zvonki/",
                "/DipvLom/periods/",
                "/DipvLom/schedule/",
                "/DipvLom/journal/",
                "/DipvLom/analytics/zvonki/",
                "/DipvLom/analytics/periods/",
                "/DipvLom/people/",
                "/DipvLom/people/teachers/",
                "/DipvLom/people/hteachers/",
                "/DipvLom/people/class/",
                "/DipvLom/people/parents/",
                "/DipvLom/people/admins/",
                "/DipvLom/tutor/kid/",
                "/DipvLom/tutor/par/",
                "/DipvLom/tutor/tea/",
                "/DipvLom/tutor/sch/",
                "/DipvLom/profiles/",
                "/DipvLom/settings/",
                "/DipvLom/request/",
                "/DipvLom/invite/",
                "/DipvLom/reauth/",
                "/DipvLom/fav32.png",
                "/DipvLom/fav16.png",
                "/DipvLom/manifest.json",
                "/DipvLom/app.js"
            ];
            // Object.getOwnPropertyNames(assets.files).map((key, i, x, val = assets.files[key]) => {
            //     urlsToCache.push(val);
            // });
        })
    // Delete old caches that are not our current one!
    this.addEventListener("activate", activateF);
    // The first time the user starts up the PWA, 'install' is triggered.
    this.addEventListener('install', installF);
    // When the webpage goes to fetch files, we intercept that request and serve up the matching files
    // if we have them
    this.addEventListener('fetch', fetchF);
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
        .then(cache => prom.then(e=> {
            console.log(urlsToCache);
            return cache.addAll(urlsToCache)
                .then(r => {
                    console.log('cached');
                    return r;
                })
                .catch(message => {
                    console.log(message)
                    for (let i of urlsToCache) {
                        try {
                            const val = cache.add(i);
                        } catch (err) {
                            console.log('sw: cache.add',i);
                        }
                    }
                })
        }))
    );
}

function fetchF(e) {
    e.respondWith(
        e.preloadResponse.then(responsePre => {
            return responsePre || getCache(e);
        }).catch(message => {
            console.log(message)
            return getCache(e);
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