document.getElementById("assistants-tbody").addEventListener('click', (e) => {
    const row = e.target.closest('[data-id]');
    if (!row) return;

    const oldId = document.getElementById("assistant-id").value;
    const newId = row.dataset.id;

    if (oldId !== newId) {
        let selectedRows = document.getElementsByClassName("selected-row");
        while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
        row.classList.add("selected-row");
        document.getElementById("assistant-id").value = newId;
    } else {
        row.classList.remove("selected-row");
        document.getElementById("assistant-id").value = "";
    }
});

document.getElementById("assistant-id").addEventListener('input', e => {
    let selectedRows = document.getElementsByClassName("selected-row");
    while (selectedRows.length > 0) selectedRows[0].classList.remove("selected-row");
    let row =  document.querySelector('#assistants-tbody *[data-id="' + e.target.value + '"]');
    if (!row) return;
    row.classList.add("selected-row");
});