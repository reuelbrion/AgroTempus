"use strict";
(function () {
    var pickImage = document.querySelector("#pick-image");
    if (pickImage) {
        pickImage.onclick = function () {
            var pick = new MozActivity({
                name: "pick",
                data: {
                    type: ["image/png", "image/jpg", "image/jpeg"],
                    // In FxOS 1.3 and before the user is allowed to crop the
                    // image by default, but this can cause out-of-memory issues
                    // so we explicitly disable it.
                    nocrop: true // don't allow the user to crop the image
                }
            });

            pick.onsuccess = function () {
                var img = document.createElement("img");
                img.src = window.URL.createObjectURL(this.result.blob);
                var imagePresenter = document.querySelector("#image-presenter");
                imagePresenter.appendChild(img);
                imagePresenter.style.display = "block";
            };

            pick.onerror = function () {
                console.log("Can't view the image");
            };
        };
    }

})();