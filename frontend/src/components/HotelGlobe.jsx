import { useEffect, useRef } from 'react';
import * as THREE from 'three';

function pointFor(lat, lon, radius = 2.05) {
  const phi = (90 - lat) * (Math.PI / 180);
  const theta = (lon + 180) * (Math.PI / 180);
  return new THREE.Vector3(
    -radius * Math.sin(phi) * Math.cos(theta),
    radius * Math.cos(phi),
    radius * Math.sin(phi) * Math.sin(theta),
  );
}

export default function HotelGlobe({ hotels = [] }) {
  const mountRef = useRef(null);

  useEffect(() => {
    const mount = mountRef.current;
    if (!mount) return undefined;
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(30, 1, 0.1, 100);
    camera.position.set(0, 0.1, 6.4);
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.setClearColor(0x000000, 0);
    mount.appendChild(renderer.domElement);

    const globe = new THREE.Group();
    scene.add(globe);
    globe.rotation.y = -0.65;

    const sphere = new THREE.Mesh(
      new THREE.SphereGeometry(1.68, 48, 48),
      new THREE.MeshPhongMaterial({ color: 0x29245f, emissive: 0x0e0b2e, shininess: 32, flatShading: true }),
    );
    globe.add(sphere);
    const wire = new THREE.LineSegments(
      new THREE.WireframeGeometry(new THREE.SphereGeometry(1.695, 16, 10)),
      new THREE.LineBasicMaterial({ color: 0x8782e7, transparent: true, opacity: 0.12 }),
    );
    globe.add(wire);
    const atmosphere = new THREE.Mesh(
      new THREE.SphereGeometry(1.77, 48, 48),
      new THREE.MeshBasicMaterial({ color: 0x5ee7d5, transparent: true, opacity: 0.045, side: THREE.BackSide }),
    );
    globe.add(atmosphere);

    const validHotels = hotels.filter((hotel) => Number.isFinite(Number(hotel.latitude)) && Number.isFinite(Number(hotel.longitude)));
    const clusters = new Map();
    validHotels.forEach((hotel) => {
      const lat = Number(hotel.latitude);
      const lon = Number(hotel.longitude);
      const key = `${Math.round(lat * 2) / 2}:${Math.round(lon * 2) / 2}`;
      const cluster = clusters.get(key) || { lat: 0, lon: 0, count: 0, unavailable: 0 };
      cluster.lat += lat; cluster.lon += lon; cluster.count += 1;
      if (hotel.available === false) cluster.unavailable += 1;
      clusters.set(key, cluster);
    });
    const points = new THREE.Group();
    clusters.forEach((cluster) => {
      const marker = new THREE.Mesh(
        new THREE.SphereGeometry(Math.min(0.035 + cluster.count * 0.008, 0.085), 10, 10),
        new THREE.MeshBasicMaterial({ color: cluster.unavailable === cluster.count ? 0xf08b9f : 0x58e0d0 }),
      );
      marker.position.copy(pointFor(cluster.lat / cluster.count, cluster.lon / cluster.count, 1.72));
      points.add(marker);
    });
    globe.add(points);

    scene.add(new THREE.AmbientLight(0x9ca8ff, 1.5));
    const keyLight = new THREE.DirectionalLight(0xffffff, 2.5);
    keyLight.position.set(4, 3, 5);
    scene.add(keyLight);

    let width = 0;
    let height = 0;
    const resize = () => {
      width = mount.clientWidth;
      height = mount.clientHeight;
      renderer.setSize(width, height, false);
      camera.aspect = width / Math.max(height, 1);
      camera.updateProjectionMatrix();
    };
    resize();
    const observer = new ResizeObserver(resize);
    observer.observe(mount);

    let dragging = false;
    let lastX = 0;
    const down = (event) => { dragging = true; lastX = event.clientX; mount.setPointerCapture?.(event.pointerId); };
    const move = (event) => {
      if (!dragging) return;
      globe.rotation.y += (event.clientX - lastX) * 0.006;
      globe.rotation.x = Math.max(-0.45, Math.min(0.45, globe.rotation.x + (event.clientY - (event.previousClientY || event.clientY)) * 0.002));
      lastX = event.clientX;
    };
    const up = () => { dragging = false; };
    mount.addEventListener('pointerdown', down);
    mount.addEventListener('pointermove', move);
    mount.addEventListener('pointerup', up);
    mount.addEventListener('pointerleave', up);

    let frame;
    const animate = () => {
      frame = requestAnimationFrame(animate);
      if (!dragging && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) globe.rotation.y += 0.0018;
      renderer.render(scene, camera);
    };
    animate();
    return () => {
      cancelAnimationFrame(frame);
      observer.disconnect();
      mount.removeEventListener('pointerdown', down);
      mount.removeEventListener('pointermove', move);
      mount.removeEventListener('pointerup', up);
      mount.removeEventListener('pointerleave', up);
      renderer.dispose();
      sphere.geometry.dispose(); sphere.material.dispose();
      wire.geometry.dispose(); wire.material.dispose();
      points.children.forEach((point) => { point.geometry.dispose(); point.material.dispose(); });
      if (mount.contains(renderer.domElement)) mount.removeChild(renderer.domElement);
    };
  }, [hotels]);

  return <div className="hotel-globe" ref={mountRef} role="img" aria-label="Interactive globe showing hotels with available coordinates" />;
}
