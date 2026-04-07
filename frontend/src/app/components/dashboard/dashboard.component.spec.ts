import { TestBed, ComponentFixture } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { OfferService } from '../../services/offer.service';
import { AuthService } from '../../services/auth.service';

describe('DashboardComponent — Responsive Design', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let component: DashboardComponent;

  beforeEach(async () => {
    const offerServiceSpy = jasmine.createSpyObj('OfferService', ['getDashboard']);
    offerServiceSpy.getDashboard.and.returnValue(of({
      activeCount: 3,
      upcomingCount: 2,
      expiredCount: 5,
      recentActive: [],
      recentUpcoming: []
    }));

    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isAdmin', 'isAuthenticated']);
    authServiceSpy.isAdmin.and.returnValue(false);
    authServiceSpy.isAuthenticated.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [DashboardComponent, RouterTestingModule, HttpClientTestingModule],
      providers: [
        { provide: OfferService, useValue: offerServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── Estructura del template ─────────────────────────────────────────────

  it('el template contiene .stats-grid con las 4 tarjetas de estadísticas', () => {
    // Feature: responsive-design, Requerimientos 3.1, 3.2, 3.3
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.stats-grid')).toBeTruthy();
    const statCards = el.querySelectorAll('.stat-card');
    expect(statCards.length).toBe(4);
  });

  it('el template contiene .offers-grid para las listas de ofertas recientes', () => {
    // Feature: responsive-design, Requerimiento 3.5
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.offers-grid')).toBeTruthy();
  });

  // ── Estilos responsivos ─────────────────────────────────────────────────

  it('los estilos contienen media query para max-width: 768px (tablet)', () => {
    // Feature: responsive-design, Property 1: cada componente define media queries para todos los breakpoints
    // Valida: Requerimientos 1.5, 3.2
    const styles = (DashboardComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('768px');
  });

  it('los estilos contienen media query para max-width: 480px (móvil)', () => {
    // Feature: responsive-design, Property 1: cada componente define media queries para todos los breakpoints
    // Valida: Requerimientos 1.4, 3.3
    const styles = (DashboardComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('480px');
  });

  it('los estilos del stats-grid usan repeat(4, 1fr) en desktop', () => {
    // Feature: responsive-design, Requerimiento 3.1
    const styles = (DashboardComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('repeat(4, 1fr)');
  });

  it('los estilos del stats-grid usan repeat(2, 1fr) en tablet', () => {
    // Feature: responsive-design, Requerimiento 3.2
    const styles = (DashboardComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('repeat(2, 1fr)');
  });

  it('los estilos del stats-grid usan 1fr en móvil', () => {
    // Feature: responsive-design, Requerimiento 3.3
    const styles = (DashboardComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('grid-template-columns: 1fr');
  });

  // ── Datos del dashboard ─────────────────────────────────────────────────

  it('muestra los conteos correctos de las tarjetas de estadísticas', () => {
    const el = fixture.nativeElement as HTMLElement;
    const statNumbers = el.querySelectorAll('.stat-number');
    expect(statNumbers.length).toBe(4);
    // activeCount=3, upcomingCount=2, expiredCount=5, total=10
    const texts = Array.from(statNumbers).map(n => n.textContent?.trim());
    expect(texts).toContain('3');
    expect(texts).toContain('2');
    expect(texts).toContain('5');
    expect(texts).toContain('10');
  });

  it('formatDate retorna string vacío para entrada vacía', () => {
    expect(component.formatDate('')).toBe('');
  });

  it('formatDate formatea una fecha ISO correctamente', () => {
    const result = component.formatDate('2024-07-15T00:00:00');
    expect(result).toContain('2024');
  });
});
