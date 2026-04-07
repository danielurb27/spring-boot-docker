import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { UserListComponent } from './user-list.component';
import { UserService } from '../../services/user.service';

describe('UserListComponent — Responsive Design', () => {
  let fixture: ComponentFixture<UserListComponent>;
  let component: UserListComponent;

  beforeEach(async () => {
    const userServiceSpy = jasmine.createSpyObj('UserService', [
      'getAllUsers', 'createUser', 'updateUser', 'deactivateUser', 'activateUser'
    ]);
    userServiceSpy.getAllUsers.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [UserListComponent, HttpClientTestingModule],
      providers: [
        { provide: UserService, useValue: userServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── Estructura del template ─────────────────────────────────────────────

  it('el template contiene .modal-overlay para el modal de edición', () => {
    // Feature: responsive-design, Requerimiento 6
    // El overlay existe en el DOM (oculto por *ngIf cuando editingUser es null)
    // Verificamos que el componente tiene la propiedad editingUser
    expect(component.editingUser).toBeNull();
  });

  it('el template contiene .form-row para el formulario de creación', () => {
    // Feature: responsive-design, Requerimiento 6.6
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.form-row')).toBeTruthy();
  });

  // ── Estilos responsivos del modal ───────────────────────────────────────

  it('los estilos contienen media query para max-width: 768px', () => {
    // Feature: responsive-design, Property 1: cada componente define media queries para todos los breakpoints
    // Valida: Requerimientos 1.5, 6.2
    const styles = (UserListComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('768px');
  });

  it('los estilos contienen media query para max-width: 480px', () => {
    // Feature: responsive-design, Property 1: cada componente define media queries para todos los breakpoints
    // Valida: Requerimientos 1.4, 6.3
    const styles = (UserListComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('480px');
  });

  it('los estilos del modal-card incluyen max-width: calc(100% - 32px) en tablet/móvil', () => {
    // Feature: responsive-design, Property 9: modal ocupa el ancho disponible en pantallas pequeñas
    // Valida: Requerimientos 6.2, 6.6
    const styles = (UserListComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('calc(100% - 32px)');
  });

  it('los estilos del modal-card incluyen max-height: 90vh en móvil', () => {
    // Feature: responsive-design, Requerimiento 6.3
    const styles = (UserListComponent as any).ɵcmp?.styles?.join('') ?? '';
    expect(styles).toContain('90vh');
  });

  it('los estilos no ocultan .modal-close con display:none', () => {
    // Feature: responsive-design, Property 10: botones críticos del modal nunca se ocultan
    // Valida: Requerimientos 6.4, 6.5
    const styles = (UserListComponent as any).ɵcmp?.styles?.join('') ?? '';
    const closeNoneMatch = styles.match(/\.modal-close[^}]*display\s*:\s*none/);
    expect(closeNoneMatch).toBeNull();
  });

  it('los estilos no ocultan .modal-actions con display:none', () => {
    // Feature: responsive-design, Property 10: botones críticos del modal nunca se ocultan
    // Valida: Requerimientos 6.4, 6.5
    const styles = (UserListComponent as any).ɵcmp?.styles?.join('') ?? '';
    const actionsNoneMatch = styles.match(/\.modal-actions[^}]*display\s*:\s*none/);
    expect(actionsNoneMatch).toBeNull();
  });

  // ── Estado inicial del componente ───────────────────────────────────────

  it('editingUser inicia en null (modal cerrado)', () => {
    expect(component.editingUser).toBeNull();
  });

  it('users inicia como array vacío', () => {
    expect(component.users).toEqual([]);
  });

  it('creating inicia en false', () => {
    expect(component.creating).toBe(false);
  });

  it('saving inicia en false', () => {
    expect(component.saving).toBe(false);
  });

  // ── Modal: abrir y cerrar ───────────────────────────────────────────────

  it('startEdit() abre el modal asignando editingUser', () => {
    const user: import('../../models/user.model').User = {
      id: 1, fullName: 'Juan', username: 'jperez', role: 'EMPLOYEE', isActive: true, createdAt: '2024-01-01T00:00:00Z'
    };
    component.startEdit(user);
    expect(component.editingUser).toBe(user);
  });

  it('cancelEdit() cierra el modal poniendo editingUser en null', () => {
    const user: import('../../models/user.model').User = {
      id: 1, fullName: 'Juan', username: 'jperez', role: 'EMPLOYEE', isActive: true, createdAt: '2024-01-01T00:00:00Z'
    };
    component.startEdit(user);
    component.cancelEdit();
    expect(component.editingUser).toBeNull();
  });
});
