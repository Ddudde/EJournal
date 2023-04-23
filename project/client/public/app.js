if ("serviceWorker" in navigator) {
    try {
        navigator.serviceWorker.register(pubUrl + "/service-worker.js")
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