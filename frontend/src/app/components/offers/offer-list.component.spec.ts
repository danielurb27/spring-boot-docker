import { TestBed, ComponentFixture } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { OfferListComponent } from './offer-list.component';
import { OfferService } from '../../services/offer.service';
import { AuthService } from '../../services/auth.service';
import { Offer, OfferStatus } from '../../models/offer.model';

/** Crea una oferta de prueba con valores por defecto */
function makeOffer(overrides: Partial<Offer> = {}): Offer {
  return {
    id: 1,
    title: 'Oferta de prueba',
    description: null,
    status: 'ACTIVA' as OfferStatus,
    offerTypeId: 1,
    sectorId: 1,
    startsAt: '2024-01-01T00:00:00',
    endsAt: '2024-12-31T23:59:59',
    createdBy: null,
    updatedBy: null,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: null,
    ...overrides
  };
}

describe('OfferListComponent — Responsive Design', () => {
  let fixture: ComponentFixture<OfferListComponent>;
  let component: OfferListComponent;

  beforeEach(async () => {
    const offerServiceSpy = jasmine.createSpyObj('OfferService', [
      'getOffers', 'getOfferTypes', 'getSectors', 'deleteOffer'
    ]);
    offerServiceSpy.getOffers.and.returnValue(of({
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: 20,
      first: true,
      last: true
    }));
    offerServiceSpy.getOfferTypes.and.returnValue(of([]));
    offerServiceSpy.getSectors.and.returnValue(of([]));

    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isAdmin', 'isAuthenticated', 'getRole']);
    authServiceSpy.isAdmin.and.returnValue(false);
    authServiceSpy.isAuthenticated.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [
        OfferListComponent,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: OfferService, useValue: offerServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}) }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OfferListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── Estructura del template ─────────────────────────────────────────────

  it('el template contiene .table-wrapper para scroll horizontal en tablet', () => {
    // Feature: responsive-design, Requerimiento 4.3
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.table-wrapper')).toBeTruthy();
  });

  it('el template contiene .offer-cards para la vista de tarjetas en móvil', () => {
    // Feature: responsive-design, Requerimiento 4.2
    // Necesitamos al menos una oferta para que el bloque *ngIf="offers.length > 0" se renderice
    component.offers = [makeOffer()];
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.offer-cards')).toBeTruthy();
  });

  it('el template contiene .filters-card para el panel de filtros', () => {
    // Feature: responsive-design, Requerimiento 4.7
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.filters-card')).toBeTruthy();
  });

  it('el template contiene .filters-grid para los controles de filtro', () => {
    // Feature: responsive-design, Requerimiento 4.7
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.filters-grid')).toBeTruthy();
  });

  // ── Tarjetas de oferta contienen todos los campos requeridos ────────────

  it('cada .offer-card contiene título, badge, fechas y acciones', () => {
    // Feature: responsive-design, Property 5: tarjetas de oferta contienen todos los campos
    // Valida: Requerimientos 4.2, 4.4
    const offers: Offer[] = [
      makeOffer({ id: 1, title: 'Oferta A', status: 'ACTIVA' }),
      makeOffer({ id: 2, title: 'Oferta B', status: 'PROXIMA' }),
      makeOffer({ id: 3, title: 'Oferta C', status: 'VENCIDA' }),
    ];
    component.offers = offers;
    fixture.detectChanges();

    const cards = fixture.nativeElement.querySelectorAll('.offer-card');
    expect(cards.length).toBe(3);

    cards.forEach((card: HTMLElement) => {
      expect(card.querySelector('.offer-title-cell')).withContext('debe tener título').toBeTruthy();
      expect(card.querySelector('.badge')).withContext('debe tener badge de estado').toBeTruthy();
      expect(card.querySelector('.offer-card-dates')).withContext('debe tener fechas').toBeTruthy();
      expect(card.querySelector('.offer-card-actions')).withContext('debe tener acciones').toBeTruthy();
    });
  });

  it('una sola oferta genera exactamente una .offer-card', () => {
    component.offers = [makeOffer({ id: 1, title: 'Única oferta' })];
    fixture.detectChanges();

    const cards = fixture.nativeElement.querySelectorAll('.offer-card');
    expect(cards.length).toBe(1);
  });

  // ── Paginación y filtros nunca se ocultan ───────────────────────────────

  it('los estilos del componente no ocultan .filters-card con display:none', () => {
    // Feature: responsive-design, Property 6: controles de navegación y filtros nunca se ocultan
    // Valida: Requerimientos 4.6, 4.7
    const styles = (OfferListComponent as any).ɵcmp?.styles?.join('') ?? '';
    // filters-card no debe tener display:none en ningún media query
    const filterNoneMatch = styles.match(/\.filters-card[^}]*display\s*:\s*none/);
    expect(filterNoneMatch).toBeNull();
  });

  // ── Métodos de utilidad ─────────────────────────────────────────────────

  it('statusLabel retorna "Activa" para ACTIVA', () => {
    expect(component.statusLabel('ACTIVA')).toBe('Activa');
  });

  it('statusLabel retorna "Próxima" para PROXIMA', () => {
    expect(component.statusLabel('PROXIMA')).toBe('Próxima');
  });

  it('statusLabel retorna "Vencida" para VENCIDA', () => {
    expect(component.statusLabel('VENCIDA')).toBe('Vencida');
  });

  it('calculateProgress retorna 0 para oferta PROXIMA', () => {
    const offer = makeOffer({ status: 'PROXIMA' });
    expect(component.calculateProgress(offer)).toBe(0);
  });

  it('calculateProgress retorna 100 para oferta VENCIDA', () => {
    const offer = makeOffer({ status: 'VENCIDA' });
    expect(component.calculateProgress(offer)).toBe(100);
  });

  it('calculateProgress retorna valor entre 0 y 100 para oferta ACTIVA', () => {
    const offer = makeOffer({
      status: 'ACTIVA',
      startsAt: '2020-01-01T00:00:00',
      endsAt: '2099-12-31T23:59:59'
    });
    const progress = component.calculateProgress(offer);
    expect(progress).toBeGreaterThanOrEqual(0);
    expect(progress).toBeLessThanOrEqual(100);
  });
});
