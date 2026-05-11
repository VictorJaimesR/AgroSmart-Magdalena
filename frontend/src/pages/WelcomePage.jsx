import { Link } from 'react-router-dom';

export default function WelcomePage() {
  return (
    <div className="landing-container">
      {/* Hero Section */}
      <section className="hero-section text-center text-white d-flex align-items-center">
        <div className="container">
          <div className="mb-4 animate__animated animate__zoomIn">
            <img
              src="/logo.png"
              alt="CampoVital Logo"
              style={{ maxWidth: '300px', height: 'auto', filter: 'drop-shadow(0 0 10px rgba(0,0,0,0.3))' }}
            />
          </div>
          <h1 className="display-4 fw-bold mb-3 animate__animated animate__fadeInDown">Tecnología que echa raíces</h1>
          <p className="lead fs-4 mb-4 animate__animated animate__fadeInUp animate__delay-1s">
            Transformando la agricultura del Magdalena con inteligencia y tecnología.
          </p>
          <div className="d-flex justify-content-center gap-3 animate__animated animate__fadeInUp animate__delay-2s">
            <Link to="/register" className="btn btn-agro btn-lg px-5 shadow">Empezar Ahora</Link>
            <Link to="/login" className="btn btn-outline-light btn-lg px-5">Iniciar Sesión</Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-5 bg-white">
        <div className="container py-5">
          <div className="text-center mb-5">
            <h2 className="fw-bold">Nuestras Funcionalidades</h2>
            <div className="bg-agro mx-auto mb-3" style={{ width: 60, height: 4 }}></div>
            <p className="text-muted">Diseñado específicamente para las necesidades del pequeño productor.</p>
          </div>

          <div className="row g-4">
            <div className="col-12 col-md-4">
              <div className="card h-100 border-0 shadow-sm text-center p-4">
                <div className="bg-success bg-opacity-10 text-success rounded-circle mx-auto mb-3 d-flex align-items-center justify-content-center" style={{ width: 80, height: 80 }}>
                  <i className="bi bi-geo-alt fs-1"></i>
                </div>
                <h5 className="fw-bold">Gestión Georreferenciada</h5>
                <p className="text-muted small">Registra tus fincas y lotes con coordenadas GPS exactas para un seguimiento preciso.</p>
              </div>
            </div>
            <div className="col-12 col-md-4">
              <div className="card h-100 border-0 shadow-sm text-center p-4">
                <div className="bg-primary bg-opacity-10 text-primary rounded-circle mx-auto mb-3 d-flex align-items-center justify-content-center" style={{ width: 80, height: 80 }}>
                  <i className="bi bi-cpu fs-1"></i>
                </div>
                <h5 className="fw-bold">Recomendaciones Inteligentes</h5>
                <p className="text-muted small">Recibe consejos técnicos basados en el estado real de tus cultivos para maximizar tu cosecha.</p>
              </div>
            </div>
            <div className="col-12 col-md-4">
              <div className="card h-100 border-0 shadow-sm text-center p-4">
                <div className="bg-warning bg-opacity-10 text-warning rounded-circle mx-auto mb-3 d-flex align-items-center justify-content-center" style={{ width: 80, height: 80 }}>
                  <i className="bi bi-cloud-lightning-rain fs-1"></i>
                </div>
                <h5 className="fw-bold">Alertas Climáticas</h5>
                <p className="text-muted small">Mantente informado sobre riesgos de sequía o inundación en tu municipio en tiempo real.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Showoff Section */}
      <section className="py-5 bg-light">
        <div className="container py-5">
          <div className="row align-items-center">
            <div className="col-lg-6 mb-4 mb-lg-0">
              <h2 className="fw-bold mb-4">Lleva el control de tu producción en el bolsillo</h2>
              <ul className="list-unstyled">
                <li className="d-flex mb-3 gap-3">
                  <i className="bi bi-check-circle-fill text-success fs-4"></i>
                  <div>
                    <h6 className="fw-bold mb-0">Modo Offline</h6>
                    <p className="text-muted small">¿Sin internet en la vereda? No hay problema. Sincroniza cuando vuelvas al pueblo.</p>
                  </div>
                </li>
                <li className="d-flex mb-3 gap-3">
                  <i className="bi bi-check-circle-fill text-success fs-4"></i>
                  <div>
                    <h6 className="fw-bold mb-0">Reportes Detallados</h6>
                    <p className="text-muted small">Exporta tus inventarios y cierres de producción en Excel para trámites bancarios.</p>
                  </div>
                </li>
                <li className="d-flex mb-3 gap-3">
                  <i className="bi bi-check-circle-fill text-success fs-4"></i>
                  <div>
                    <h6 className="fw-bold mb-0">Soporte Multimedia</h6>
                    <p className="text-muted small">Sube fotos de tus cultivos para llevar un registro visual de su crecimiento.</p>
                  </div>
                </li>
              </ul>
            </div>
            <div className="col-lg-6 text-center">
               <div className="bg-agro rounded shadow-lg p-3" style={{ transform: 'rotate(2deg)' }}>
                  <img src="https://images.unsplash.com/photo-1523348837708-15d4a09cfac2?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"
                    className="img-fluid rounded shadow-sm" alt="App Preview" />
               </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-5 bg-agro text-white text-center">
        <div className="container py-4">
          <h2 className="fw-bold mb-4">¿Eres un agricultor del Magdalena?</h2>
          <p className="lead mb-4">Únete a cientos de productores que ya están optimizando sus fincas con CampoVital.</p>
          <Link to="/register" className="btn btn-light btn-lg px-5 shadow">Regístrate Gratis</Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-4 bg-dark text-white text-center">
        <div className="container">
          <p className="mb-0 small opacity-50">&copy; 2026 CampoVital Magdalena — Todos los derechos reservados.</p>
        </div>
      </footer>

      <style>{`\n        .landing-container {\n          background-color: #f8f9fa;\n        }\n        .hero-section {\n          background: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)), \n                      url('https://images.unsplash.com/photo-1500382017468-9049fed747ef?ixlib=rb-1.2.1&auto=format&fit=crop&w=1600&q=80');\n          background-size: cover;\n          background-position: center;\n          height: 80vh;\n          min-height: 500px;\n        }\n        .bg-agro { background-color: #2c6e49 !important; }\n        .text-agro { color: #2c6e49 !important; }\n        .btn-agro {\n          background-color: #2c6e49;\n          color: white;\n          border: none;\n        }\n        .btn-agro:hover {\n          background-color: #1e4d32;\n          color: white;\n        }\n        .animate__animated {\n          animation-duration: 1s;\n          animation-fill-mode: both;\n        }\n        @keyframes fadeInDown {\n          from { opacity: 0; transform: translate3d(0, -50px, 0); }\n          to { opacity: 1; transform: translate3d(0, 0, 0); }\n        }\n        @keyframes fadeInUp {\n          from { opacity: 0; transform: translate3d(0, 50px, 0); }\n          to { opacity: 1; transform: translate3d(0, 0, 0); }\n        }\n        .animate__fadeInDown { animation-name: fadeInDown; }\n        .animate__fadeInUp { animation-name: fadeInUp; }\n        .animate__delay-1s { animation-delay: 0.5s; }\n        .animate__delay-2s { animation-delay: 1s; }\n      `}</style>
    </div>
  );
}
