const p = new URLSearchParams(window.location.search);
if (p.has('error'))      document.getElementById('error-msg').classList.add('visible');
if (p.has('registered')) document.getElementById('success-msg').classList.add('visible');