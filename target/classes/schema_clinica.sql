-- =============================================================
--  Clinica2.0 — "Más Cerca de Dios"
--  Esquema H2 (idempotente: IF NOT EXISTS + MERGE INTO)
-- =============================================================

-- Emisor (datos de la clínica para cabeceras)
CREATE TABLE IF NOT EXISTS upeu_emisor (
    id_emisor IDENTITY NOT NULL,
    ruc VARCHAR(20) NOT NULL,
    nombre_comercial VARCHAR(80) NOT NULL,
    ubigeo VARCHAR(10) NOT NULL,
    domicilio_fiscal VARCHAR(120) NOT NULL,
    urbanizacion VARCHAR(60) NOT NULL,
    departamento VARCHAR(40) NOT NULL,
    provincia VARCHAR(40) NOT NULL,
    distrito VARCHAR(40) NOT NULL,
    CONSTRAINT upeu_emisor_pk PRIMARY KEY (id_emisor)
);

-- Perfiles
CREATE TABLE IF NOT EXISTS upeu_perfil (
    id_perfil IDENTITY NOT NULL,
    nombre VARCHAR(30) NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    CONSTRAINT upeu_perfil_pk PRIMARY KEY (id_perfil)
);

-- Usuarios
CREATE TABLE IF NOT EXISTS upeu_usuario (
    id_usuario IDENTITY NOT NULL,
    usuario VARCHAR(30) NOT NULL,
    clave VARCHAR(60) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    id_perfil INTEGER NOT NULL,
    id_referencia BIGINT NULL,
    CONSTRAINT upeu_usuario_pk PRIMARY KEY (id_usuario)
);

-- Paciente
CREATE TABLE IF NOT EXISTS upeu_paciente (
    id_paciente IDENTITY NOT NULL,
    dni VARCHAR(12) NOT NULL,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    telefono VARCHAR(20),
    fecha_nacimiento DATE,
    sexo VARCHAR(10),
    direccion VARCHAR(120),
    email VARCHAR(80),
    CONSTRAINT upeu_paciente_pk PRIMARY KEY (id_paciente)
);

-- Especialidad
CREATE TABLE IF NOT EXISTS upeu_especialidad (
    id_especialidad IDENTITY NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    descripcion VARCHAR(200),
    CONSTRAINT upeu_especialidad_pk PRIMARY KEY (id_especialidad)
);

-- Médico
CREATE TABLE IF NOT EXISTS upeu_medico (
    id_medico IDENTITY NOT NULL,
    dni VARCHAR(12) NOT NULL,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    num_colegiatura VARCHAR(20),
    telefono VARCHAR(20),
    email VARCHAR(80),
    id_especialidad INTEGER NOT NULL,
    CONSTRAINT upeu_medico_pk PRIMARY KEY (id_medico)
);

-- Horario
CREATE TABLE IF NOT EXISTS upeu_horario (
    id_horario IDENTITY NOT NULL,
    id_medico INTEGER NOT NULL,
    dia_semana VARCHAR(5) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CONSTRAINT upeu_horario_pk PRIMARY KEY (id_horario),
    CONSTRAINT chk_dia_semana CHECK (dia_semana IN ('LUN','MAR','MIE','JUE','VIE','SAB','DOM'))
);

-- Enfermero
CREATE TABLE IF NOT EXISTS upeu_enfermero (
    id_enfermero IDENTITY NOT NULL,
    dni VARCHAR(12) NOT NULL,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    telefono VARCHAR(20),
    CONSTRAINT upeu_enfermero_pk PRIMARY KEY (id_enfermero)
);

-- Cita
CREATE TABLE IF NOT EXISTS upeu_cita (
    id_cita IDENTITY NOT NULL,
    num_ticket VARCHAR(30) NOT NULL,
    id_paciente INTEGER NOT NULL,
    id_medico INTEGER NOT NULL,
    id_especialidad INTEGER NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    tipo_atencion VARCHAR(20) NOT NULL,
    motivo VARCHAR(255),
    fecha_reg TIMESTAMP NOT NULL,
    id_usuario_reg INTEGER,
    CONSTRAINT upeu_cita_pk PRIMARY KEY (id_cita),
    CONSTRAINT chk_estado_cita CHECK (estado IN ('PROGRAMADA','EN_ESPERA','TRIAJE','EN_CONSULTA','ATENDIDA','CANCELADA')),
    CONSTRAINT chk_tipo_atencion CHECK (tipo_atencion IN ('PROGRAMADA','ORDEN_LLEGADA','EMERGENCIA'))
);

-- Triaje (signos vitales)
CREATE TABLE IF NOT EXISTS upeu_triaje (
    id_triaje IDENTITY NOT NULL,
    id_cita INTEGER NOT NULL,
    id_enfermero INTEGER,
    presion_sistolica DOUBLE,
    presion_diastolica DOUBLE,
    temperatura DOUBLE,
    frec_cardiaca INTEGER,
    peso DOUBLE,
    talla DOUBLE,
    motivo_consulta VARCHAR(255),
    observaciones VARCHAR(500),
    fecha_reg TIMESTAMP NOT NULL,
    CONSTRAINT upeu_triaje_pk PRIMARY KEY (id_triaje)
);

-- Consulta
CREATE TABLE IF NOT EXISTS upeu_consulta (
    id_consulta IDENTITY NOT NULL,
    id_cita INTEGER NOT NULL,
    sintomas VARCHAR(500),
    diagnostico VARCHAR(500),
    observaciones VARCHAR(500),
    examenes_solicitados VARCHAR(500),
    fecha_reg TIMESTAMP NOT NULL,
    CONSTRAINT upeu_consulta_pk PRIMARY KEY (id_consulta)
);

-- Receta
CREATE TABLE IF NOT EXISTS upeu_receta (
    id_receta IDENTITY NOT NULL,
    id_consulta INTEGER NOT NULL,
    indicaciones_generales VARCHAR(500),
    recomendaciones VARCHAR(500),
    fecha_reg TIMESTAMP NOT NULL,
    CONSTRAINT upeu_receta_pk PRIMARY KEY (id_receta)
);

-- Detalle de receta
CREATE TABLE IF NOT EXISTS upeu_receta_detalle (
    id_receta_detalle IDENTITY NOT NULL,
    id_receta INTEGER NOT NULL,
    medicamento VARCHAR(120) NOT NULL,
    dosis VARCHAR(60),
    frecuencia VARCHAR(60),
    duracion VARCHAR(60),
    via VARCHAR(30),
    CONSTRAINT upeu_receta_detalle_pk PRIMARY KEY (id_receta_detalle)
);

-- ============================
--  FOREIGN KEYS
-- ============================
ALTER TABLE upeu_usuario
    ADD CONSTRAINT IF NOT EXISTS upeu_perfil_usuario_fk
    FOREIGN KEY (id_perfil) REFERENCES upeu_perfil (id_perfil);

ALTER TABLE upeu_medico
    ADD CONSTRAINT IF NOT EXISTS upeu_especialidad_medico_fk
    FOREIGN KEY (id_especialidad) REFERENCES upeu_especialidad (id_especialidad);

ALTER TABLE upeu_horario
    ADD CONSTRAINT IF NOT EXISTS upeu_medico_horario_fk
    FOREIGN KEY (id_medico) REFERENCES upeu_medico (id_medico);

ALTER TABLE upeu_cita
    ADD CONSTRAINT IF NOT EXISTS upeu_paciente_cita_fk
    FOREIGN KEY (id_paciente) REFERENCES upeu_paciente (id_paciente);

ALTER TABLE upeu_cita
    ADD CONSTRAINT IF NOT EXISTS upeu_medico_cita_fk
    FOREIGN KEY (id_medico) REFERENCES upeu_medico (id_medico);

ALTER TABLE upeu_cita
    ADD CONSTRAINT IF NOT EXISTS upeu_especialidad_cita_fk
    FOREIGN KEY (id_especialidad) REFERENCES upeu_especialidad (id_especialidad);

ALTER TABLE upeu_cita
    ADD CONSTRAINT IF NOT EXISTS upeu_usuario_cita_fk
    FOREIGN KEY (id_usuario_reg) REFERENCES upeu_usuario (id_usuario);

ALTER TABLE upeu_triaje
    ADD CONSTRAINT IF NOT EXISTS upeu_cita_triaje_fk
    FOREIGN KEY (id_cita) REFERENCES upeu_cita (id_cita);

ALTER TABLE upeu_triaje
    ADD CONSTRAINT IF NOT EXISTS upeu_enfermero_triaje_fk
    FOREIGN KEY (id_enfermero) REFERENCES upeu_enfermero (id_enfermero);

ALTER TABLE upeu_consulta
    ADD CONSTRAINT IF NOT EXISTS upeu_cita_consulta_fk
    FOREIGN KEY (id_cita) REFERENCES upeu_cita (id_cita);

ALTER TABLE upeu_receta
    ADD CONSTRAINT IF NOT EXISTS upeu_consulta_receta_fk
    FOREIGN KEY (id_consulta) REFERENCES upeu_consulta (id_consulta);

ALTER TABLE upeu_receta_detalle
    ADD CONSTRAINT IF NOT EXISTS upeu_receta_detalle_fk
    FOREIGN KEY (id_receta) REFERENCES upeu_receta (id_receta);

-- ============================
--  DATOS SEMILLA (MERGE INTO)
-- ============================

MERGE INTO upeu_perfil (id_perfil, nombre, codigo) KEY(id_perfil) VALUES
  (1, 'Root', 'ROOT'),
  (2, 'Administrador', 'ADM'),
  (3, 'Recepcionista', 'REC'),
  (4, 'Medico', 'MED'),
  (5, 'Enfermero', 'ENF');

MERGE INTO upeu_emisor (id_emisor, ruc, nombre_comercial, ubigeo, domicilio_fiscal, urbanizacion, departamento, provincia, distrito)
  KEY(id_emisor) VALUES
  (1, '20601234567', 'Clínica Más Cerca de Dios', '210101', 'Av. Independencia 123', 'Centro', 'Puno', 'San Román', 'Juliaca');

MERGE INTO upeu_especialidad (id_especialidad, nombre, descripcion) KEY(id_especialidad) VALUES
  (1, 'Medicina General', 'Atención primaria y consulta general'),
  (2, 'Pediatría', 'Atención de niños y adolescentes'),
  (3, 'Cardiología', 'Diagnóstico y tratamiento del corazón'),
  (4, 'Traumatología', 'Lesiones del aparato locomotor'),
  (5, 'Dermatología', 'Enfermedades de la piel'),
  (6, 'Ginecología', 'Salud reproductiva femenina'),
  (7, 'Neurología', 'Enfermedades del sistema nervioso'),
  (8, 'Oftalmología', 'Enfermedades de los ojos');

MERGE INTO upeu_medico (id_medico, dni, nombres, apellidos, num_colegiatura, telefono, email, id_especialidad)
  KEY(id_medico) VALUES
  (1, '40123456', 'Carlos', 'Ramos Apaza', 'CMP-12345', '951000111', 'cramos@clinica.pe', 1),
  (2, '40234567', 'Lucía',  'Torres Vilca', 'CMP-22345', '951000222', 'ltorres@clinica.pe', 2),
  (3, '40345678', 'Mario',  'Villca Mamani','CMP-32345', '951000333', 'mvillca@clinica.pe', 3),
  (4, '40456789', 'Ana',    'Gutiérrez Puma', 'CMP-42345', '951000444', 'agutierrez@clinica.pe', 4),
  (5, '40567890', 'Pedro',  'Huanca Quispe', 'CMP-52345', '951000555', 'phuanca@clinica.pe', 5),
  (6, '40678901', 'María',  'Condori Flores', 'CMP-62345', '951000666', 'mcondori@clinica.pe', 6),
  (7, '40789012', 'Jorge',  'Mamani Calla', 'CMP-72345', '951000777', 'jmamani@clinica.pe', 7),
  (8, '40890123', 'Rosa',   'Apaza Ticona', 'CMP-82345', '951000888', 'rapaza@clinica.pe', 8);

MERGE INTO upeu_enfermero (id_enfermero, dni, nombres, apellidos, telefono) KEY(id_enfermero) VALUES
  (1, '70123456', 'Rosa', 'López Quispe', '951000444');

MERGE INTO upeu_usuario (id_usuario, usuario, clave, estado, id_perfil, id_referencia) KEY(id_usuario) VALUES
  (1, 'admin',     'admin123',  'ACTIVO', 1, NULL),
  (2, 'recep',     'recep123',  'ACTIVO', 3, NULL),
  (3, 'doc.ramos', 'medico123', 'ACTIVO', 4, 1),
  (4, 'enf.lopez', 'enfer123',  'ACTIVO', 5, 1);

MERGE INTO upeu_paciente (id_paciente, dni, nombres, apellidos, telefono, fecha_nacimiento, sexo, direccion, email)
  KEY(id_paciente) VALUES
  (1, '43631917', 'Carlos', 'Mamani Condori', '951111222', '1985-03-12', 'MASCULINO', 'Jr. Lima 100, Juliaca',  'carlos.mamani@correo.pe'),
  (2, '52198430', 'Lucía',  'Quispe Apaza',   '951222333', '1990-07-05', 'FEMENINO',  'Av. El Sol 250, Juliaca','lucia.quispe@correo.pe'),
  (3, '61234567', 'Jorge',  'Apaza Mamani',   '951333444', '1978-11-20', 'MASCULINO', 'Jr. Bolívar 88, Juliaca','jorge.apaza@correo.pe');

MERGE INTO upeu_cita (id_cita, num_ticket, id_paciente, id_medico, id_especialidad, fecha, hora, estado, tipo_atencion, motivo, fecha_reg, id_usuario_reg)
  KEY(id_cita) VALUES
  (1, 'T-20260526-0001', 1, 1, 1, '2026-05-26', '09:00:00', 'PROGRAMADA', 'PROGRAMADA', 'Chequeo general', CURRENT_TIMESTAMP, 2),
  (2, 'T-20260526-0002', 2, 2, 2, '2026-05-26', '10:00:00', 'PROGRAMADA', 'PROGRAMADA', 'Control pediátrico', CURRENT_TIMESTAMP, 2);

-- ============================
--  Reinicio de IDENTITY tras seeds
--  El RESTART real se hace dinámicamente en DatabaseConfig.syncIdentitySequences()
--  para evitar colisiones cuando ya hay registros con id ≥ valor fijo.
-- ============================
