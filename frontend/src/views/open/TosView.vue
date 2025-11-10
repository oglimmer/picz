<template>
  <!-- Modern Navigation -->
  <nav class="navbar">
    <div class="container">
      <a class="logo" href="/">
        <img src="@/assets/logo-full.jpg" alt="PicZ" class="logo-img" />
        <span>Pic Z</span>
      </a>

      <div class="nav-links">
        <a href="/">Home</a>
        <a href="/tos">Tos</a>
        <a href="/imprint">Imprint</a>
        <a href="/support">Support</a>
        <a href="/login" class="login-btn">Login</a>
      </div>
    </div>
  </nav>

  <!-- Content Section -->
  <section class="content-section">
    <div class="container">
      <h1 class="page-title">Terms and Conditions</h1>
      <p class="subtitle">Last Updated: {{ lastUpdated }}</p>

      <div class="content-wrapper" v-html="tosContent"></div>
    </div>
  </section>

  <!-- Footer -->
  <footer>
    <div class="container">
      <div class="footer-content">
        <div class="footer-logo">
          <img src="@/assets/logo-full.jpg" alt="PicZ" class="logo-img-small" />
          <span>PicZ</span>
        </div>
        <div class="footer-links">
          <a href="/tos">Tos</a>
          <a href="/privacy">Privacy</a>
          <a href="/imprint">Imprint</a>
          <a href="/support">Support</a>
        </div>
      </div>
      <p class="copyright">
        © {{ new Date().getFullYear() }} PicZ. All rights reserved.
      </p>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { useTosContent } from "@/composables/useTosContent";

const { tosContent, lastUpdated } = useTosContent();
</script>

<style scoped>
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

a {
  text-decoration: none;
  color: inherit;
}

/* Navbar */
.navbar {
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 1rem 0;
}

.navbar .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  font-weight: 700;
  font-size: 1.5rem;
  color: #333;
}

.logo-img {
  height: 32px;
  width: auto;
  margin-right: 0.5rem;
  border-radius: 4px;
}

.logo-img-small {
  height: 24px;
  width: auto;
  margin-right: 0.5rem;
  border-radius: 4px;
}

.nav-links {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.nav-links a {
  font-weight: 500;
  transition: all 0.3s ease;
  color: #333;
}

.nav-links a:hover {
  color: #3a86ff;
}

.login-btn {
  background-color: #3a86ff;
  color: white !important;
  padding: 0.5rem 1.5rem;
  border-radius: 50px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background-color: #2667cc;
  transform: translateY(-2px);
}

/* Content Section */
.content-section {
  margin-top: 70px;
  padding: 5rem 0;
  background-color: #f8f9fa;
  min-height: calc(100vh - 70px);
}

.page-title {
  text-align: center;
  margin-bottom: 1rem;
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
}

.subtitle {
  text-align: center;
  margin-bottom: 3rem;
  font-size: 1.1rem;
  color: #777;
}

.content-wrapper {
  background-color: white;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.04);
  max-width: 800px;
  margin: 0 auto;
  line-height: 1.7;
}

.intro-text {
  font-size: 1.1rem;
  color: #555;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3a86ff;
}

.content-wrapper h2 {
  color: #333;
  font-size: 1.4rem;
  font-weight: 600;
  margin: 2rem 0 1rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #3a86ff;
}

.content-wrapper p {
  margin-bottom: 1rem;
  color: #555;
}

.content-wrapper a {
  color: #3a86ff;
  transition: all 0.3s ease;
}

.content-wrapper a:hover {
  color: #2667cc;
  text-decoration: underline;
}

/* Footer */
footer {
  background-color: #212529;
  color: white;
  padding: 3rem 0 2rem;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.footer-logo {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.footer-links {
  display: flex;
  gap: 1.5rem;
}

.footer-links a {
  opacity: 0.8;
  transition: all 0.3s ease;
}

.footer-links a:hover {
  opacity: 1;
}

.copyright {
  text-align: center;
  opacity: 0.5;
  margin-top: 2rem;
  font-size: 0.9rem;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .nav-links {
    gap: 1rem;
  }

  .page-title {
    font-size: 2rem;
  }

  .content-wrapper {
    padding: 2rem;
  }

  .footer-content {
    flex-direction: column;
    gap: 2rem;
  }

  .footer-links {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }

  .content-wrapper {
    padding: 1.5rem;
  }

  .container {
    padding: 0 1rem;
  }
}
</style>
