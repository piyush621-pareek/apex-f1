/* ============================================
   APEX F1 — Shared JS
   Runs on every page: active nav link + mobile menu
   ============================================ */

document.addEventListener('DOMContentLoaded', () => {
  // Highlight the nav link matching the current page
  const currentPage = document.body.dataset.page;
  document.querySelectorAll('.nav-link').forEach((link) => {
    if (link.dataset.page === currentPage) {
      link.classList.add('active');
    }
  });

  // Mobile nav toggle
  const toggle = document.querySelector('.nav-toggle');
  const links = document.querySelector('.nav-links');

  if (toggle && links) {
    toggle.addEventListener('click', () => {
      links.classList.toggle('open');
    });

    // Close menu when a link is tapped
    links.querySelectorAll('.nav-link').forEach((link) => {
      link.addEventListener('click', () => links.classList.remove('open'));
    });
  }
});
