# Agents — Skills de Integración AppLogger

Este directorio contiene **guías de dominio** para agentes de IA y desarrolladores que necesitan integrar AppLogger en proyectos externos.

El skill canónico para discovery estándar de Agent Skills está en:
`.github/skills/applogger-kmp-integration/SKILL.md`

El archivo `docs/ES/agents/SKILL.md` se conserva como referencia documental en esta sección.

| Skill | Plataforma | Archivo |
|---|---|---|
| Skill principal compatible Agent Skills | Android + iOS (KMP) | [../../../.github/skills/applogger-kmp-integration/SKILL.md](../../../.github/skills/applogger-kmp-integration/SKILL.md) |
| Integrar AppLogger en app Android (Kotlin) | Android Mobile + TV | [android-integration.md](android-integration.md) |
| Integrar AppLogger en iOS (KMP puro) | iOS 14+ | [ios-integration.md](ios-integration.md) |

---

## Cómo usar estos skills

Cada skill contiene:
1. **Checklist de prerrequisitos** — lo que la app destino necesita antes de empezar.
2. **Pasos de integración** — código exacto, verificado contra el source del SDK.
3. **Patrones de uso** — ejemplos reales de logging por nivel.
4. **Errores comunes** — problemas conocidos y cómo resolverlos.

Los ejemplos de código están verificados contra el SDK versión `0.1.0-alpha.1`.
