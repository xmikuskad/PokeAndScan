const email = ['falconsockar', 'gmail.com'].join('@');
const link = document.getElementById('privacy-email');

if (link) {
  link.href = `mailto:${email}`;
  link.textContent = email;
}
