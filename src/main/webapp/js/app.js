/* Progressive enhancement only: the site remains fully usable with JavaScript disabled. */
(function () {
  'use strict';

  // Quantity +/- buttons: adjust the number, then submit the form (server re-validates against stock).
  document.addEventListener('click', function (e) {
    var btn = e.target.closest('[data-step]');
    if (!btn) return;
    var form = btn.closest('form');
    var input = form.querySelector('input[name="quantity"]');
    var next = (parseInt(input.value, 10) || 1) + parseInt(btn.dataset.step, 10);
    var min = parseInt(input.min, 10) || 1;
    var max = parseInt(input.max, 10) || Infinity;
    if (next < min || next > max) return;
    input.value = next;
    form.submit();
  });

  // Typing a quantity and pressing Enter / leaving the field submits the change.
  document.addEventListener('change', function (e) {
    if (e.target.classList.contains('qty-input')) e.target.form.submit();
  });

  // Destructive forms ask for confirmation first.
  document.addEventListener('submit', function (e) {
    var msg = e.target.dataset.confirm;
    if (msg && !window.confirm(msg)) e.preventDefault();
  });

  // Success/info flash messages fade away on their own; errors stay until dismissed.
  document.querySelectorAll('[data-autodismiss].alert-success, [data-autodismiss].alert-info').forEach(function (el) {
    setTimeout(function () {
      if (window.bootstrap) bootstrap.Alert.getOrCreateInstance(el).close();
    }, 5000);
  });
})();
