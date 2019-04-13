document.getElementById("equipment-switch").addEventListener('change', e => {
    const weaponRows = document.getElementsByClassName('weapon-rows');
    const transportRows = document.getElementsByClassName('transport-rows');
    if (e.target.checked) {
        for (let i = 0; i < weaponRows.length; ++i) weaponRows[i].classList.remove("hidden");
        for (let i = 0; i < transportRows.length; ++i) transportRows[i].classList.add("hidden");
    } else {
        for (let i = 0; i < weaponRows.length; ++i) weaponRows[i].classList.add("hidden");
        for (let i = 0; i < transportRows.length; ++i) transportRows[i].classList.remove("hidden");
    }
});