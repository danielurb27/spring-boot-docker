/**
 * main.ts — Punto de entrada de la aplicación Angular.
 *
 * ¿Qué hace este archivo?
 * Es el equivalente al main() de Java. Angular arranca desde aquí.
 * bootstrapApplication() inicializa la aplicación con el componente raíz
 * y la configuración global (rutas, interceptores, etc.).
 *
 * Angular 17 usa "Standalone Components" por defecto.
 * En versiones anteriores, se usaba NgModule para organizar la app.
 * Con standalone, cada componente declara sus propias dependencias,
 * lo que hace el código más modular y fácil de entender.
 */
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig)
  .catch(err => console.error(err));
