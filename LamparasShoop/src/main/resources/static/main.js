// ===============================
// VARIABLES GLOBALES
// ===============================
let productos = [];
let productosFiltrados = [];

const contenedorProductos = document.querySelector("#contenedor-productos");
const tituloPrincipal = document.querySelector("#titulo-principal");

const inputBusqueda = document.querySelector("#busquedaNombre");
const selectCategoria = document.querySelector("#busquedaCategoria");
const selectOrden = document.querySelector("#ordenarProductos");
const inputPrecioMin = document.querySelector("#precioMin");
const inputPrecioMax = document.querySelector("#precioMax");
const btnFiltrar = document.querySelector("#btnFiltrar");
const btnLimpiar = document.querySelector("#btnLimpiar");
const botonesCategoterias = document.querySelectorAll('.boton-categoria');

let botonesAgregar = document.querySelectorAll(".agregar-carrito");
let numerito = document.querySelector("#numerito");

// ===============================
// CARGAR PRODUCTOS DESDE EL BACKEND
// ===============================
async function cargarProductos() {
    try {
        // ✅ Llamamos al endpoint JSON correcto
        const respuesta = await fetch("/productos/api");
        if (!respuesta.ok) throw new Error("Error al obtener productos del servidor");

        productos = await respuesta.json();
        productosFiltrados = productos;
        mostrarProductos(productos);
    } catch (error) {
        console.error("Error:", error);
        contenedorProductos.innerHTML = `<p>Error al cargar los productos.</p>`;
    }
}

// ===============================
// MOSTRAR PRODUCTOS EN EL HTML
// ===============================
function mostrarProductos(lista) {
    contenedorProductos.innerHTML = "";

    if (lista.length === 0) {
        contenedorProductos.innerHTML = `<p>No se encontraron productos.</p>`;
        return;
    }

    lista.forEach(prod => {
        const div = document.createElement("div");
        div.classList.add("producto");
        div.innerHTML = `
            <div class="producto-detalle">
                <img src="data:image/png;base64,${prod.imagen}" alt="${prod.nombre}" width="200px" height="200px">
                <h3>${prod.nombre}</h3>
                <p>$${prod.precio}</p>
                <div class="producto-botones">
                    <button class="mas-info" data-id="${prod.id}">
                        <i class="bi bi-info-circle"></i> Más información
                    </button>
                    <button class="agregar-carrito" data-id="${prod.id}">
                        <i class="bi bi-cart-plus"></i>
                    </button>
                </div>
            </div>
        `;
        contenedorProductos.appendChild(div);
        actualizarBotonesInfo();
    });

    actualizarBotonesAgregar();
}

// ===============================
// BOTONES DE AGREGAR AL CARRITO
// ===============================
function actualizarBotonesAgregar() {
    botonesAgregar = document.querySelectorAll(".agregar-carrito");

    botonesAgregar.forEach(boton => {
        boton.addEventListener("click", agregarAlCarrito);
    });
}

async function agregarAlCarrito(e) {
    const idProducto = parseInt(e.currentTarget.dataset.id);
    const boton = e.currentTarget; // Guardar referencia antes de cualquier await

    try {
        const respuesta = await fetch("/api/carrito/agregar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                productoId: idProducto,
                cantidad: 1
            })
        });

        if (!respuesta.ok) {
            throw new Error("Error al agregar producto al carrito");
        }

        // Actualizar el numerito después de agregar
        await actualizarNumerito();

        // Mostrar feedback visual (boton ya está guardado arriba)
        const textoOriginal = boton.innerHTML;
        boton.innerHTML = '<i class="bi bi-check-circle"></i>';
        boton.disabled = true;

        setTimeout(() => {
            boton.innerHTML = textoOriginal;
            boton.disabled = false;
        }, 1000);

    } catch (error) {
        console.error("Error:", error);
        alert("Error al agregar el producto al carrito");
    }
}

async function actualizarNumerito() {
    try {
        const respuesta = await fetch("/api/carrito/cantidad");
        if (!respuesta.ok) throw new Error("Error al obtener cantidad");

        const data = await respuesta.json();
        numerito.innerText = data.cantidad;
    } catch (error) {
        console.error("Error al actualizar numerito:", error);
        numerito.innerText = "0";
    }
}

// ===============================
// FILTROS Y BUSQUEDA
// ===============================
function filtrarProductos() {
    const nombre = inputBusqueda.value.toLowerCase();
    const categoria = selectCategoria.value;
    const precioMin = parseFloat(inputPrecioMin.value) || 0;
    const precioMax = parseFloat(inputPrecioMax.value) || Infinity;

    productosFiltrados = productos.filter(p => {
        const coincideNombre = p.nombre.toLowerCase().includes(nombre);
        const coincideCategoria = categoria === "todos" || p.categoria === categoria;
        const coincidePrecio = p.precio >= precioMin && p.precio <= precioMax;
        return coincideNombre && coincideCategoria && coincidePrecio;
    });

    ordenarProductos();
    mostrarProductos(productosFiltrados);
}

function ordenarProductos() {
    const orden = selectOrden.value;

    switch (orden) {
        case "precioAsc":
            productosFiltrados.sort((a, b) => a.precio - b.precio);
            break;
        case "precioDesc":
            productosFiltrados.sort((a, b) => b.precio - a.precio);
            break;
        case "nombreAsc":
            productosFiltrados.sort((a, b) => a.nombre.localeCompare(b.nombre));
            break;
        case "nombreDesc":
            productosFiltrados.sort((a, b) => b.nombre.localeCompare(a.nombre));
            break;
        default:
            break;
    }
}

botonesCategoterias.forEach(boton => {
    boton.addEventListener('click', (e) => {
        botonesCategoterias.forEach(boton => boton.classList.remove('active'));
        e.currentTarget.classList.add('active');

        if (e.currentTarget.id !== "todos") {
            const productoCategoria = productos.find(producto => producto.categoria === e.currentTarget.id);
            tituloPrincipal.innerText = productoCategoria.categoria;

            const productosBoton = productos.filter(producto => producto.categoria === e.currentTarget.id);
            mostrarProductos(productosBoton);
        } else {
            tituloPrincipal.innerText = "Todos los productos";
            cargarProductos(productos);
        }
    })
})

function limpiarFiltros() {
    inputBusqueda.value = "";
    selectCategoria.value = "todos";
    selectOrden.value = "ninguno";
    inputPrecioMin.value = "";
    inputPrecioMax.value = "";
    productosFiltrados = productos;
    mostrarProductos(productos);
}

// ===============================
// EVENTOS
// ===============================
btnFiltrar.addEventListener("click", filtrarProductos);
btnLimpiar.addEventListener("click", limpiarFiltros);

// ===============================
// INICIO
// ===============================
document.addEventListener("DOMContentLoaded", () => {
    cargarProductos();
    actualizarNumerito();
});

let puntuacionSeleccionada = 0;

async function mostrarModal(producto) {
    const modal = document.querySelector("#modalProducto");
    const modalImg = document.querySelector("#modal-imagen");
    const modalNombre = document.querySelector("#modal-nombre");
    const modalDescripcion = document.querySelector("#modal-descripcion");
    const modalPrecio = document.querySelector("#modal-precio");
    const modalCategoria = document.querySelector("#modal-categoria");
    const modalReseñas = document.querySelector("#modal-reseñas");
    const btnAgregarModal = document.querySelector("#modal-agregar-carrito");
    const formReseña = document.querySelector("#formReseña");

    // Llenar info básica
    modalImg.src = `data:image/png;base64,${producto.imagen}`;
    modalNombre.textContent = producto.nombre;
    modalDescripcion.textContent = producto.descripcion || "Sin descripción disponible.";
    modalPrecio.textContent = producto.precio;
    modalCategoria.textContent = producto.categoria;

    // Resetear puntuación y formulario
    puntuacionSeleccionada = 0;
    formReseña.reset();

    // Cargar reseñas
    modalReseñas.innerHTML = `<p>Cargando reseñas...</p>`;
    await cargarReseñas(producto.id, modalReseñas);

    // Mostrar modal
    modal.style.display = "block";
    btnAgregarModal.dataset.id = producto.id;
    formReseña.dataset.id = producto.id;

    // Cerrar modal
    const cerrar = document.querySelector(".cerrar-modal");
    cerrar.onclick = () => modal.style.display = "none";
    window.onclick = (e) => { if (e.target === modal) modal.style.display = "none"; };

    // Inicializar estrellas interactivas
    inicializarEstrellas();

    // Evento para enviar reseña
    formReseña.onsubmit = async (e) => {
        e.preventDefault();
        await enviarReseña(producto.id);
        await cargarReseñas(producto.id, modalReseñas);
        formReseña.reset();
        puntuacionSeleccionada = 0;
        resaltarEstrellas(0);
    };
}

async function cargarReseñas(idProducto, contenedor) {
    try {
        const resp = await fetch(`/api/resenas/producto/${idProducto}`);
        if (!resp.ok) throw new Error("Error al obtener reseñas");
        const reseñas = await resp.json();

        if (reseñas.length === 0) {
            contenedor.innerHTML = `<p>No hay reseñas para este producto aún.</p>`;
        } else {
            contenedor.innerHTML = "";
            reseñas.forEach(r => {
                const div = document.createElement("div");
                div.classList.add("reseña");
                div.innerHTML = `
                    <strong>${r.usuario}</strong> 
                    <small>(${r.fecha})</small><br>
                    <span>${"⭐".repeat(r.puntuacion)}</span>
                    <p>${r.comentario}</p>
                `;
                contenedor.appendChild(div);
            });
        }
    } catch (error) {
        contenedor.innerHTML = `<p>Error al cargar reseñas.</p>`;
        console.error(error);
    }
}

function inicializarEstrellas() {
    const estrellas = document.querySelectorAll("#estrellas span");
    estrellas.forEach(est => {
        est.addEventListener("click", () => {
            puntuacionSeleccionada = parseInt(est.dataset.value);
            resaltarEstrellas(puntuacionSeleccionada);
        });
    });
}

function resaltarEstrellas(valor) {
    const estrellas = document.querySelectorAll("#estrellas span");
    estrellas.forEach(est => {
        est.style.color = parseInt(est.dataset.value) <= valor ? "gold" : "gray";
    });
}

async function enviarReseña(idProducto) {
    const nombre = document.querySelector("#reseña-nombre").value.trim();
    console.log(nombre);
    const comentario = document.querySelector("#reseña-comentario").value.trim();

    if (!nombre || !comentario || puntuacionSeleccionada === 0) {
        alert("Por favor completa todos los campos y selecciona una puntuación.");
        return;
    }

    try {
        const respuesta = await fetch(`/api/resenas/${idProducto}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                usuario: nombre,
                comentario,
                puntuacion: puntuacionSeleccionada,
                fecha: new Date().toISOString().split("T")[0]
            })
        });
        console.log(respuesta);
        if (!respuesta.ok) throw new Error("Error al enviar reseña");
        alert("✅ Reseña enviada correctamente.");
    } catch (error) {
        console.error("Error:", error);
        alert("❌ No se pudo enviar la reseña.");
    }
}

function actualizarBotonesInfo() {
    const botonesInfo = document.querySelectorAll(".mas-info");

    botonesInfo.forEach(boton => {
        boton.addEventListener("click", (e) => {
            const idProducto = e.currentTarget.dataset.id;
            const producto = productos.find(p => p.id == idProducto);
            if (producto) mostrarModal(producto);
        });
    });
}