if ("serviceWorker" in navigator) {
    try {
        navigator.serviceWorker.addEventListener('message', event => {
            // event is a MessageEvent object
            console.log(`The service worker sent me a message: ${event.data}`);
        });
        navigator.serviceWorker.register("/DipvLom/service-worker.js")
            .then(data => {
                if (data.installing) {
                    console.log("Service worker installing");
                } else if (data.waiting) {
                    console.log("Service worker installed");
                } else if (data.active) {
                    console.log("Service worker active");
                }
                return navigator.serviceWorker.ready;
            })
            .then(data => {
                if (data.installing) {
                    console.log("Service worker installing");
                } else if (data.waiting) {
                    console.log("Service worker installed");
                } else if (data.active) {
                    console.log("Service worker active");
                }
            });
    } catch (error) {
        console.error(`Registration failed with ${error}`);
    }
} else {
    console.log('service worker is not supported');
}