<script setup lang="ts">
import { ref } from "vue";

const isNavOpen = ref(false);
const toggleNav = () => {
  isNavOpen.value = !isNavOpen.value;
};
</script>

<template>
  <!-- Modern Navigation -->
  <nav class="navbar">
    <div class="container">
      <a class="logo" href="/">
        <img src="@/assets/logo-full.jpg" alt="PicZ" class="logo-img" />
        <span>Pic Z</span>
      </a>

      <div class="nav-toggle" @click="toggleNav">
        <span></span><span></span><span></span>
      </div>

      <div class="nav-links" :class="{ active: isNavOpen }">
        <a href="/tos">Tos</a>
        <a href="/imprint">Imprint</a>
        <a href="/support">Support</a>
        <a href="/login" class="login-btn">Login</a>
      </div>
    </div>
  </nav>

  <!-- Hero Section -->
  <section class="hero">
    <div class="container">
      <div class="hero-content">
        <h1>
          Share vacation moments, <span>without the social media hassle</span>
        </h1>
        <p class="subtitle">
          Beautiful, private photo galleries for your travel memories. No
          accounts needed for viewers.
        </p>
        <a href="/login" class="cta-button">Get Started</a>
      </div>
    </div>
  </section>

  <!-- Features Section -->
  <section class="features">
    <div class="container">
      <h2 class="section-title">Why choose PicZ?</h2>

      <div class="feature-grid">
        <div class="feature-card">
          <div class="icon">📍</div>
          <h3>Location Context</h3>
          <p>
            Add maps, coordinates and location details to each photo, creating a
            journey viewers can follow
          </p>
        </div>

        <div class="feature-card">
          <div class="icon">🔗</div>
          <h3>Simple Sharing</h3>
          <p>
            Just send a link - no accounts needed for your friends and family to
            view your memories
          </p>
        </div>

        <div class="feature-card">
          <div class="icon">📝</div>
          <h3>Rich Storytelling</h3>
          <p>
            Organize photos into sections with descriptions to craft the perfect
            travel narrative
          </p>
        </div>

        <div class="feature-card">
          <div class="icon">🛡️</div>
          <h3>Privacy First</h3>
          <p>
            No social media algorithm deciding who sees your photos - you
            control exactly who gets access
          </p>
        </div>
      </div>
    </div>
  </section>

  <!-- How It Works -->
  <section class="how-it-works">
    <div class="container">
      <h2 class="section-title">Simple as 1-2-3</h2>

      <div class="steps">
        <div class="step">
          <div class="step-number">1</div>
          <h3>Upload your photos</h3>
          <p>Select and upload vacation photos in batches - fast and simple</p>
        </div>

        <div class="step">
          <div class="step-number">2</div>
          <h3>Add context</h3>
          <p>Organize into sections, tag locations, add descriptions</p>
        </div>

        <div class="step">
          <div class="step-number">3</div>
          <h3>Share privately</h3>
          <p>Send a link to friends and family - no accounts or apps needed</p>
        </div>
      </div>

      <div class="cta-center">
        <a href="/login" class="cta-button">Start Sharing Now</a>
      </div>
    </div>
  </section>

  <!-- Testimonial/Final CTA -->
  <section class="final-cta">
    <div class="container">
      <h2>Ready to share your travel memories in a meaningful way?</h2>
      <p>
        Join the thousands of travelers we built this for—and be the first to
        experience a better way to share your adventures.
      </p>
      <a href="/login" class="cta-button cta-button-alt"
        >Get Started - It's Free</a
      >
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

section {
  padding: 5rem 0;
}

.section-title {
  text-align: center;
  margin-bottom: 3rem;
  font-size: 2.5rem;
  font-weight: 700;
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

.nav-toggle {
  display: none;
  flex-direction: column;
  gap: 6px;
  cursor: pointer;
}

.nav-toggle span {
  width: 30px;
  height: 2px;
  background-color: #333;
  transition: all 0.3s ease;
}

/* Hero Section */
.hero {
  height: 55vh;
  min-height: 600px;
  display: flex;
  align-items: center;
  position: relative;
  margin-top: 70px;
  overflow: hidden;
}

.hero-content {
  max-width: 650px;
  padding: 2rem 0;
}

.hero h1 {
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 1.5rem;
}

.hero h1 span {
  color: #3a86ff;
}

.subtitle {
  font-size: 1.3rem;
  color: #777;
  margin-bottom: 2.5rem;
}

.cta-button {
  display: inline-block;
  background-color: #3a86ff;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  padding: 1rem 2.5rem;
  border-radius: 50px;
  transition: all 0.3s ease;
  border: none;
  text-align: center;
  box-shadow: 0 4px 15px rgba(58, 134, 255, 0.3);
}

.cta-button:hover {
  background-color: #2667cc;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(58, 134, 255, 0.4);
}

.cta-button-alt {
  background-color: white;
  color: #3a86ff;
  border: 2px solid #3a86ff;
  box-shadow: none;
}

.cta-button-alt:hover {
  background-color: #3a86ff;
  color: white;
  box-shadow: 0 8px 20px rgba(58, 134, 255, 0.4);
}

/* Features Section */
.features {
  background-color: #f8f9fa;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-card {
  background-color: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.feature-card .icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

/* How it works section */
.steps {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 2rem;
  margin-bottom: 4rem;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 280px;
}

.step-number {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #3a86ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
}

.step h3 {
  font-size: 1.4rem;
  margin-bottom: 1rem;
}

.cta-center {
  text-align: center;
  margin-top: 3rem;
}

/* Final CTA Section */
.final-cta {
  background-color: #3a86ff;
  color: white;
  text-align: center;
  padding: 5rem 0;
}

.final-cta h2 {
  font-size: 2.5rem;
  margin-bottom: 1.5rem;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.final-cta p {
  font-size: 1.2rem;
  margin-bottom: 2.5rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
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
  .nav-toggle {
    display: flex;
  }

  .nav-links {
    position: absolute;
    top: 72px;
    left: 0;
    right: 0;
    background-color: white;
    flex-direction: column;
    padding: 2rem;
    box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
    transform: translateY(-150%);
    opacity: 0;
    transition: all 0.3s ease;
    align-items: flex-start;
    z-index: -1;
  }

  .nav-links.active {
    transform: translateY(0);
    opacity: 1;
    z-index: 900;
  }

  .hero h1 {
    font-size: 2.5rem;
  }

  .section-title {
    font-size: 2rem;
  }

  .footer-content {
    flex-direction: column;
    gap: 2rem;
  }

  .footer-links {
    flex-wrap: wrap;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .hero h1 {
    font-size: 2rem;
  }

  .subtitle {
    font-size: 1.1rem;
  }

  section {
    padding: 3rem 0;
  }
}
</style>
