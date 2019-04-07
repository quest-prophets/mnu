document.getElementById("add-button").addEventListener('click', () => {
    pressButton(document.getElementById("add-button"));
    unhideInterface(document.getElementById("add-interface"));
});

document.getElementById("edit-button").addEventListener('click', () => {
    pressButton(document.getElementById("edit-button"));
    unhideInterface(document.getElementById("edit-interface"));
});

document.getElementById("find-button").addEventListener('click', () => {
    pressButton(document.getElementById("find-button"));
    unhideInterface(document.getElementById("find-interface"));
});

document.getElementById("reward-button").addEventListener('click', () => {
    pressButton(document.getElementById("reward-button"));
    unhideInterface(document.getElementById("reward-interface"));
});

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

//Dates formatting
dates = document.getElementsByClassName("td_date");
for (let i = 0; i < dates.length; ++i) {
    const date = new Date(dates[i].innerText);
    const day = date.getDate().length === 2 ? date.getDate() : "0" + date.getDate();
    const month = (date.getMonth() + 1).length === 2 ? date.getMonth() + 1 : "0" + (date.getMonth() + 1);
    const year = date.getFullYear();
    dates[i].innerText = day + "." + month + "." + year;
}