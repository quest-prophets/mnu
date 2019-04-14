document.getElementsByTagName('body')[0].addEventListener('click', async e => {
    const action = e.target.dataset.action;
    if (action === 'add-to-cart') {
        const response = await post("/manufacturer/cart/modify", {
            type: e.target.dataset.type,
            id: e.target.dataset.id,
            quantity: 1
        });
        const json = await response.json();
        if (json.error) document.getElementById("message").className = 'flash flash_error';
        else document.getElementById("message").className = 'flash flash_success';
        document.getElementById('message').innerText = json.message;
    }
});

function post(url, json) {
    return fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(json)
    })
}