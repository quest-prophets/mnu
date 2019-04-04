function setAddInterface() {
    pressButton(document.getElementById("add-button"));
    unhideInterface(document.getElementById("add-interface"));
}

function setEditInterface() {
    pressButton(document.getElementById("edit-button"));
    unhideInterface(document.getElementById("edit-interface"));
}

function setFindInterface() {
    pressButton(document.getElementById("find-button"));
    unhideInterface(document.getElementById("find-interface"));
}

function setRewardInterface() {
    pressButton(document.getElementById("reward-button"));
    unhideInterface(document.getElementById("reward-interface"));
}

function unhideInterface(unhidden_interface) {
    document.getElementById("add-interface").classList.add("hidden");
    document.getElementById("edit-interface").classList.add("hidden");
    document.getElementById("find-interface").classList.add("hidden");
    document.getElementById("reward-interface").classList.add("hidden");
    unhidden_interface.classList.remove("hidden");
}

function pressButton(pressed_button) {
    document.getElementById("add-button").classList.add("button_unpressed");
    document.getElementById("edit-button").classList.add("button_unpressed");
    document.getElementById("find-button").classList.add("button_unpressed");
    document.getElementById("reward-button").classList.add("button_unpressed");
    document.getElementById("add-button").classList.remove("button_pressed");
    document.getElementById("edit-button").classList.remove("button_pressed");
    document.getElementById("find-button").classList.remove("button_pressed");
    document.getElementById("reward-button").classList.remove("button_pressed");
    pressed_button.classList.add("button_pressed");
    pressed_button.classList.remove("button_unpressed");
}