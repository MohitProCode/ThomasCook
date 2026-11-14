import { useRef, useState } from 'react';
import { CaretLeft, CaretRight } from '@phosphor-icons/react';

function prefersReducedMotion() {
  return typeof window !== 'undefined'
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

export default function PhotoCarousel({ photos = [], name = 'Hotel' }) {
  const trackRef = useRef(null);
  const [index, setIndex] = useState(0);

  if (photos.length < 2) return null;

  function handleScroll() {
    const track = trackRef.current;
    if (!track || track.clientWidth === 0) return;
    setIndex(Math.min(photos.length - 1, Math.max(0, Math.round(track.scrollLeft / track.clientWidth))));
  }

  function goTo(i) {
    const track = trackRef.current;
    if (!track) return;
    track.scrollTo({ left: i * track.clientWidth, behavior: prefersReducedMotion() ? 'auto' : 'smooth' });
  }

  return (
    <div className="photo-carousel" role="region" aria-roledescription="carousel" aria-label={`${name} photos`}>
      <div className="carousel-track" ref={trackRef} onScroll={handleScroll}>
        {photos.map((src, i) => (
          <div
            className="carousel-slide"
            key={src}
            role="group"
            aria-roledescription="slide"
            aria-label={`${i + 1} of ${photos.length}`}
          >
            <img src={src} alt={`${name} photo ${i + 1}`} loading={i === 0 ? 'eager' : 'lazy'} draggable="false" />
          </div>
        ))}
      </div>
      <button
        type="button"
        className="carousel-btn carousel-prev"
        aria-label="Previous photo"
        onClick={() => goTo(index - 1 < 0 ? photos.length - 1 : index - 1)}
      >
        <CaretLeft size={20} weight="bold" aria-hidden="true" />
      </button>
      <button
        type="button"
        className="carousel-btn carousel-next"
        aria-label="Next photo"
        onClick={() => goTo((index + 1) % photos.length)}
      >
        <CaretRight size={20} weight="bold" aria-hidden="true" />
      </button>
      <p className="carousel-counter" aria-hidden="true">{index + 1} / {photos.length}</p>
      <div className="carousel-dots" aria-hidden="true">
        {photos.map((src, i) => (
          <span key={src} className={i === index ? 'dot active' : 'dot'} />
        ))}
      </div>
    </div>
  );
}
