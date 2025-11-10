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
      <h1 class="page-title">Imprint / Impressum</h1>
      <p class="subtitle">Legal information and contact details</p>

      <div class="content-wrapper">
        <div class="legal-info">
          <p class="intro-text">For Germany: Angaben gemäß § 5 TMG</p>

          <div class="provider-info">
            <h2>Website Provider</h2>
            <p class="provider-description">
              This website is provided by / Diese Webseite wird bereitgestellt
              von:
            </p>

            <div class="contact-card">
              <h3>Oliver Zimpasser</h3>
              <div class="contact-details">
                <p><strong>Address:</strong></p>
                <p>
                  Berliner Str. 39a<br />
                  63110 Rodgau<br />
                  Germany
                </p>

                <p><strong>Email:</strong></p>
                <p>
                  <a
                    href="mail&#116;o&#58;%6F&#103;li%6&#68;%&#54;D&#37;65r&#64;g&#109;ail&#46;com"
                    class="email-link"
                    >&#111;glimm&#101;&#114;&#64;gmai&#108;&#46;com</a
                  >
                </p>
              </div>
            </div>
          </div>

          <div class="service-info">
            <h2>Service Information</h2>
            <p class="service-note">
              All services are provided free of charge.
            </p>
          </div>
        </div>
      </div>
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

<script setup lang="ts"></script>

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
  max-width: 700px;
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
  font-style: italic;
}

.provider-info,
.service-info {
  margin-bottom: 2rem;
}

.provider-info h2,
.service-info h2 {
  color: #333;
  font-size: 1.6rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #3a86ff;
}

.provider-description {
  color: #555;
  margin-bottom: 1.5rem;
  font-size: 1.1rem;
}

.contact-card {
  background-color: #f8f9fa;
  padding: 2rem;
  border-radius: 12px;
  border: 1px solid #e9ecef;
  margin-bottom: 1rem;
}

.contact-card h3 {
  color: #3a86ff;
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  text-align: center;
}

.contact-details p {
  margin-bottom: 1rem;
  color: #555;
}

.contact-details strong {
  color: #333;
  font-weight: 600;
}

.email-link {
  color: #3a86ff;
  transition: all 0.3s ease;
}

.email-link:hover {
  color: #2667cc;
  text-decoration: underline;
}

.service-note {
  color: #555;
  font-size: 1.1rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3a86ff;
  margin: 0;
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

  .contact-card {
    padding: 1.5rem;
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

  .contact-card {
    padding: 1rem;
  }
}
</style>
