$('button#cancel').on('click', function (e) {
    e.preventDefault();
    window.history.back();
    return false;
});