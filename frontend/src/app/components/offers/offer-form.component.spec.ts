import { TestBed, ComponentFixture } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { OfferFormComponent } from './offer-form.component';
import { OfferService } from '../../services/offer.service';

describe('OfferFormComponent — Responsive Design', () => {
  let fixture: ComponentFixture<OfferFormComponent>;
  let component: OfferFormComponent;

  beforeEach(async () => {
    const offerServiceSpy = jasmine.createSpyObj('OfferService', [
      'getOfferTypes', 'getSectors', 'getOfferById', 'createOffer', 'updateOffer'
    ]);
    offerServiceSpy.getOfferTypes.and.returnValue(of([]));
    offerServiceSpy.getSectors.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [
        OfferFormComponent,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: OfferService, useValue: offerServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => null } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OfferFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── Estructura del template ─────────────────────────────────────────────

  it('el template contiene .form-row para el layout de dos columnas', () => {
    // Feature: responsive-design, Requerimiento 5.1
    const el = fixture.nativeElement as HTMLElement;
    const formRows = el.querySelectorAll('.form-row');
    expect(formRows.length).toBeGreaterThan(0);
  });

  it('el template contiene .form-actions para los botones de acción', () => {
    // Feature: responsive-design, Requerimiento 5.4
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.form-actions')).toBeTruthy();
  });

  it('el template contiene el botón de submit', () => {
    const el = fixture.nativeElement as HTMLElement;
    const submitBtn = el.querySelector('button[type="submit"]');
    expect(submitBtn).toBeTruthy();
  });

  // ── Estilos responsivos ─────────────────────────────────────────────────

  it('los estilos contienen media query para max-width: 480px', () => {
    // Feature: responsive-design, Property 1: cada componente define media queries para todos los breakpoints
    // Valida: Requerimientos 1.4, 5.2
    const styles = (OfferFormComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('480px');
  });

  it('los estilos colapsan .form-row a una columna en móvil', () => {
    // Feature: responsive-design, Property 7: campos del formulario colapsan a una columna en móvil
    // Valida: Requerimientos 5.2, 5.3
    const styles = (OfferFormComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('grid-template-columns: 1fr');
  });

  it('los estilos no ocultan .alert con display:none', () => {
    // Feature: responsive-design, Property 8: mensajes de error nunca se ocultan
    // Valida: Requerimiento 5.5
    const styles = (OfferFormComponent as any).ɵcmp?.styles?.join('') ?? '';
    const alertNoneMatch = styles.match(/\.alert[^}]*display\s*:\s*none/);
    expect(alertNoneMatch).toBeNull();
  });

  // ── Estado inicial del componente ───────────────────────────────────────

  it('isEditing inicia en false para nueva oferta', () => {
    expect(component.isEditing).toBe(false);
  });

  it('saving inicia en false', () => {
    expect(component.saving).toBe(false);
  });

  it('errorMessage inicia vacío', () => {
    expect(component.errorMessage).toBe('');
  });

  it('dateError inicia vacío', () => {
    expect(component.dateError).toBe('');
  });
});
