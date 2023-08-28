$(document).ready(function() {


function pollServer() {
  // Perform an AJAX request to the server
  var xhr = new XMLHttpRequest();
  xhr.open('GET', '/api/auth/auth-biometric', true);
  xhr.onreadystatechange = function() {
    if (xhr.readyState === XMLHttpRequest.DONE) {
      if (xhr.status === 200) {
        window.location.href = '/api/content/index';
              }
    }
  };
  xhr.send();
}

setInterval(pollServer, 4000);


  $('#otpForm').submit(function(event) {
    event.preventDefault();

    var formData = JSON.stringify({
      otp: $('input[name="otp"]').val(),
    });

    $.ajax({
      url: '/api/auth/otp',
      type: 'POST',
      contentType: 'application/json',
      data: formData,
      success: function() {
        window.location.href = '/api/content/index';
      },
      error: function() {
              window.location.href = '/api/auth/login?message=' + encodeURIComponent("Invalid OTP.");
      }
    });
  });
});

