<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* CAMBIADO: Fondo oscuro en lugar de gradiente */
  background-color: var(--bg-primary);
  padding: 20px;
}

.not-found-content {
  text-align: center;
  color: var(--text-primary); /* CAMBIADO */
}

.not-found-content h1 {
  font-size: 120px;
  font-weight: 700;
  margin: 0;
  line-height: 1;
  color: var(--accent-primary); /* CAMBIADO: Para darle un toque de color */
}

.not-found-content h2 {
  font-size: 32px;
  font-weight: 600;
  margin: 20px 0;
}

.not-found-content p {
  font-size: 18px;
  margin-bottom: 32px;
  color: var(--text-secondary); /* CAMBIADO */
}

.btn-home {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: var(--accent-primary); /* CAMBIADO */
  color: white; /* CAMBIADO */
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  transition: transform 0.2s, background-color 0.2s;
}

.btn-home:hover {
  transform: translateY(-2px);
  background: var(--accent-primary-hover); /* CAMBIADO */
}
</style>
