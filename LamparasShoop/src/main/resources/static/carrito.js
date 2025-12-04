// Cargar productos del carrito desde la API
let productosEnCarrito = [];

// Referencias a elementos del DOM
const contenedorCarritoVacio = document.querySelector("#carrito-vacio");
const contenedorCarritoProductos = document.querySelector("#carrito-productos");
const contenedorCarritoAcciones = document.querySelector("#carrito-acciones");
const contenedorCarritoComprado = document.querySelector("#carrito-comprado");
let botonesEliminar = document.querySelectorAll(".carrito-producto-eliminar");
const botonVaciar = document.querySelector("#carrito-acciones-vaciar");
const botonComprar = document.querySelector("#carrito-acciones-comprar");
const contenedorTotal = document.querySelector("#total");

// Cargar productos en el carrito desde la API
async function cargarProductosCarrito() {
    try {
        const respuesta = await fetch("/api/carrito");
        if (!respuesta.ok) throw new Error("Error al obtener carrito");

        productosEnCarrito = await respuesta.json();

        if (productosEnCarrito && productosEnCarrito.length > 0) {
            contenedorCarritoVacio.classList.add("disabled");
            contenedorCarritoProductos.classList.remove("disabled");
            contenedorCarritoAcciones.classList.remove("disabled");
            contenedorCarritoComprado.classList.add("disabled");

            contenedorCarritoProductos.innerHTML = "";

            productosEnCarrito.forEach(item => {
                const div = document.createElement("div");
                div.classList.add("carrito-producto");

                // Convertir byte array a base64 para la imagen
                const imagenBase64 = btoa(
                    new Uint8Array(item.imagen).reduce((data, byte) => data + String.fromCharCode(byte), '')
                );

                div.innerHTML = `
                    <img class="carrito-producto-imagen" src="data:image/png;base64,${imagenBase64}" alt="${item.nombre}">
                    <div class="carrito-producto-titulo">
                        <small>Nombre</small>
                        <h3>${item.nombre}</h3>
                    </div>
                    <div class="carrito-producto-cantidad">
                        <small>Cantidad</small>
                        <p>${item.cantidad}</p>
                    </div>
                    <div class="carrito-producto-precio">
                        <small>Precio</small>
                        <p>$${item.precio}</p>
                    </div>
                    <div class="carrito-producto-subtotal">
                        <small>Subtotal</small>
                        <p>$${item.precio * item.cantidad}</p>
                    </div>
                    <button class="carrito-producto-eliminar" id="${item.id}">
                        <i class="bi bi-trash-fill"></i>
                    </button>
                `;
                contenedorCarritoProductos.append(div);
            });

            actualizarBotonesEliminar();
            actualizarTotal();

        } else {
            contenedorCarritoVacio.classList.remove("disabled");
            contenedorCarritoProductos.classList.add("disabled");
            contenedorCarritoAcciones.classList.add("disabled");
            contenedorCarritoComprado.classList.add("disabled");
        }
    } catch (error) {
        console.error("Error al cargar carrito:", error);
        contenedorCarritoVacio.classList.remove("disabled");
        contenedorCarritoProductos.classList.add("disabled");
        contenedorCarritoAcciones.classList.add("disabled");
    }
}

// Eliminar producto individual usando API
function actualizarBotonesEliminar() {
    botonesEliminar = document.querySelectorAll(".carrito-producto-eliminar");

    botonesEliminar.forEach(boton => {
        boton.addEventListener("click", eliminarDelCarrito);
    });
}

async function eliminarDelCarrito(e) {
    const idItem = parseInt(e.currentTarget.id);

    try {
        const respuesta = await fetch(`/api/carrito/eliminar/${idItem}`, {
            method: "DELETE"
        });

        if (!respuesta.ok) throw new Error("Error al eliminar item");

        await cargarProductosCarrito();
    } catch (error) {
        console.error("Error:", error);
        alert("Error al eliminar el producto del carrito");
    }
}

// Vaciar carrito completo usando API
botonVaciar.addEventListener("click", async () => {
    try {
        const respuesta = await fetch("/api/carrito/vaciar", {
            method: "DELETE"
        });

        if (!respuesta.ok) throw new Error("Error al vaciar carrito");

        await cargarProductosCarrito();
    } catch (error) {
        console.error("Error:", error);
        alert("Error al vaciar el carrito");
    }
});

// Actualizar total
function actualizarTotal() {
    const totalCalculado = productosEnCarrito.reduce(
        (acc, item) => acc + (item.precio * item.cantidad),
        0
    );
    contenedorTotal.innerText = `$${totalCalculado}`;
}

async function comprar() {
    if (productosEnCarrito.length === 0) return;

    const numeroTelefono = "3006795246";
    let textoProductos = productosEnCarrito
        .map(item => `${item.nombre} (x${item.cantidad})`)
        .join("%0A");

    const mensaje = `Hola 👋, me gustaría comprar los siguientes productos:%0A${textoProductos}%0A📍Estoy interesado/a en concretar la compra. Gracias.`;

    window.location.href = `https://api.whatsapp.com/send?phone=${numeroTelefono}&text=${mensaje}`;

    // Vaciar carrito después de comprar
    try {
        await fetch("/api/carrito/vaciar", { method: "DELETE" });
        await cargarProductosCarrito();
    } catch (error) {
        console.error("Error al vaciar carrito:", error);
    }
}

botonComprar.addEventListener("click", comprar);

// Cargar carrito al iniciar
cargarProductosCarrito();
