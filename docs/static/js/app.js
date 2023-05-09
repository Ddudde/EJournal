async function init() {
    if ("serviceWorker" in navigator) {
        try {
            navigator.serviceWorker.addEventListener('message', event => {
                console.log(`The service worker sent me a message: ${event.data}`);
            });
            const reg = await navigator.serviceWorker.register("/DipvLom/service-worker.js")
            if (reg.installing) {
                console.log("Service worker installing");
            } else if (reg.waiting) {
                console.log("Service worker installed");
            } else if (reg.active) {
                console.log("Service worker active");
            }
            reg.addEventListener("updatefound", e => {
                if (reg.installing) {
                    const worker = reg.installing;
                    worker.addEventListener("statechange", e1 => {
                        if(worker.state == "installed") {
                            console.log("install complete");
                            if(navigator.serviceWorker.controller) {
                                console.log("New content is available and will be used all",
                                    "tabs for this page are closed.");
                            } else {
                                console.log("Content is cached for offline use.");
                            }
                        } else if (worker.state == "installing") {
                            console.log("the install event has fired, but not yet complete");
                        } else if (worker.state == "activating") {
                            console.log("the activate event has fired, but not yet complete");
                        } else if (worker.state == "activated") {
                            console.log("fully active");
                        } else if (worker.state == "redundant") {
                            console.log("discarded. Either failed install, or it's been replaced by a newer version");
                        }
                    });
                }
            });
            if(!localStorage.getItem("notifToken")) {
                console.log("try notif app");
                firebase.initializeApp({
                    messagingSenderId: "781991460409",
                    apiKey: "AIzaSyBrH7xUOxnVjhFGeVTIM9gZB0kxr-2xOwc",
                    projectId: "e-journalfcm",
                    appId: "1:781991460409:web:a900bf500869ddd6f097e8"
                });
                const messaging = firebase.messaging();
                messaging.useServiceWorker(reg);
                requestPerm(messaging);
            }
        } catch (error) {
            console.log(`Registration failed with ${error}`);
        }
    } else {
        console.log('service worker is not supported');
    }
}

init();

async function requestPerm(messaging) {
    try {
        console.log("try request perm");
        await messaging.requestPermission();
        const token = await messaging.getToken();
        console.log('Your token is:', token);
        localStorage.setItem("notifToken", token);

        return token;
    } catch (error) {
        console.log(error);
    }
}