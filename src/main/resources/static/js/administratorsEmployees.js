
function setAddInterface() {
    unhideInterface(document.getElementById("add-interface"));
}

function setEditInterface() {
    unhideInterface(document.getElementById("edit-interface"));
}

function setFindInterface() {
    unhideInterface(document.getElementById("find-interface"));
}

function setCashInterface() {
    unhideInterface(document.getElementById("cash-interface"));
}

function unhideInterface(unhidden_interface) {
    document.getElementById("add-interface").classList.add("hidden");
    document.getElementById("edit-interface").classList.add("hidden");
    document.getElementById("find-interface").classList.add("hidden");
    document.getElementById("cash-interface").classList.add("hidden");
    unhidden_interface.classList.remove("hidden");
}