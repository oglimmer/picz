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
      <h1 class="page-title">Support & FAQ</h1>
      <p class="subtitle">Get help and find answers to common questions</p>

      <div class="content-wrapper">
        <!-- FAQ Section -->
        <div class="faq-section">
          <h2>Frequently Asked Questions</h2>

          <div class="faq-item">
            <h3>How much storage space do I have?</h3>
            <p>
              Each user has 50 MB of storage for free. Additional storage space
              plans will come in the future.
            </p>
          </div>
        </div>

        <!-- Getting Started Section -->
        <div class="getting-started-section">
          <h2>Getting Started</h2>
          <p class="intro-text">
            The application is easy to use. You can create albums and upload
            images. You can share each album with a simple web link to anyone
            having a browser. The idea of the application is to share vacation
            pictures while providing enough context like maps and date
            information.
          </p>

          <div class="step-guide">
            <div class="step">
              <h3>1. Sign In</h3>
              <p>
                You can login via "Sign in with Apple", Google, Discord or
                Github
              </p>
              <div class="image-container">
                <img :src="login_png" alt="Login screen" />
              </div>
            </div>

            <div class="step">
              <h3>2. Create an Album</h3>
              <p>
                The first thing you have to do is create a new album. Click on
                "Add Album" on the top right corner.
              </p>
              <div class="image-container">
                <img :src="album_list_png" alt="Album list view" />
              </div>
            </div>

            <div class="step">
              <h3>3. Add Photos</h3>
              <p>
                Once you have clicked on the new album, you can add images to
                the album. Click "Select Multiple Photos" at the bottom of the
                screen.
              </p>
              <div class="image-container">
                <img :src="new_album_png" alt="New album view" />
              </div>
            </div>

            <div class="step">
              <h3>4. Upload Images</h3>
              <p>
                Select all images you want to add to the album and click "Add"
                at the top right corner.
              </p>
              <div class="image-container">
                <img :src="add_images_png" alt="Add images interface" />
              </div>
            </div>

            <div class="step">
              <h3>5. Wait for Processing</h3>
              <p>Wait until all pictures are uploaded and processed.</p>
              <div class="image-container">
                <img :src="image_list_png" alt="Image list view" />
              </div>
            </div>

            <div class="step">
              <h3>6. Edit & Organize</h3>
              <p>
                You can click on an image to add a description, rotate the
                image, add a map at the geo coordinates of the image, add a
                section slide or delete the image.
              </p>
              <div class="image-container">
                <img :src="image_details_png" alt="Image details view" />
              </div>
            </div>
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

<script setup lang="ts">
import login_png from "@/assets/login.png";
import new_album_png from "@/assets/new-album.png";
import add_images_png from "@/assets/add-images.png";
import album_list_png from "@/assets/album-list.png";
import image_details_png from "@/assets/image-details.png";
import image_list_png from "@/assets/image-list.png";
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
  max-width: 1000px;
  margin: 0 auto;
  line-height: 1.7;
}

.faq-section,
.getting-started-section {
  margin-bottom: 3rem;
}

.faq-section h2,
.getting-started-section h2 {
  color: #333;
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 2rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #3a86ff;
}

.faq-item {
  background-color: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  border-left: 4px solid #3a86ff;
}

.faq-item h3 {
  color: #333;
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.faq-item p {
  color: #555;
  margin: 0;
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

.step-guide {
  display: grid;
  gap: 2rem;
}

.step {
  background-color: #f8f9fa;
  padding: 2rem;
  border-radius: 8px;
  border-left: 4px solid #3a86ff;
}

.step h3 {
  color: #333;
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.step p {
  color: #555;
  margin-bottom: 1.5rem;
}

.image-container {
  text-align: center;
  margin-top: 1rem;
}

.image-container img {
  max-width: 100%;
  height: auto;
  max-height: 400px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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

  .image-container img {
    max-height: 300px;
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

  .step {
    padding: 1.5rem;
  }

  .image-container img {
    max-height: 250px;
  }
}
</style>
