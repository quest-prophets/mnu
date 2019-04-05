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

function pressButton(button_to_press) {
    let pressed_buttons = document.getElementsByClassName("button_pressed");
    while (pressed_buttons.length > 0) pressed_buttons[0].classList.remove("button_pressed");
    button_to_press.classList.add("button_pressed");
}