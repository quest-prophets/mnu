document.getElementById("experiments-button").addEventListener('click', function () {
    pressButton(document.getElementById("experiments-button"));
    document.getElementById("new-article-button").classList.add("hidden");
    document.getElementById("new-experiment-button").classList.remove("hidden")
});

document.getElementById("articles-button").addEventListener('click', function () {
    pressButton(document.getElementById("articles-button"));
    document.getElementById("new-experiment-button").classList.add("hidden");
    document.getElementById("new-article-button").classList.remove("hidden")
});


function pressButton(button_to_press) {
    let pressed_buttons = document.getElementsByClassName("button_pressed");
    while (pressed_buttons.length > 0) pressed_buttons[0].classList.remove("button_pressed");
    button_to_press.classList.add("button_pressed");
}