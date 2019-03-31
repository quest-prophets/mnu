function post(url, body = '{}') {
    return fetch(url, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    });
}

function get(url) {
    return fetch(url, {
        method: 'GET',
        credentials: 'same-origin'
    });
}

async function postRegister(credentials) {
    const response = await post('/auth/register', credentials);
    const json = await response.json();
    return json.message;
}