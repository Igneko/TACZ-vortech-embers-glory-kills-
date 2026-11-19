package com.vortech.glorykills.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Valores ajustables del mod. Se generan en config/vortech_embers-common.toml
 * la primera vez que se abre el juego.
 */
public final class GloryKillsConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue EXECUTION_HEALTH_THRESHOLD;
    public static final ForgeConfigSpec.IntValue EXECUTION_WINDOW_TICKS;
    public static final ForgeConfigSpec.DoubleValue EXECUTION_RANGE;
    public static final ForgeConfigSpec.DoubleValue BLEED_OUT_DEATH_CHANCE;
    public static final ForgeConfigSpec.IntValue EXECUTION_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue EXECUTION_IMPACT_TICK;

    public static final ForgeConfigSpec.DoubleValue HEAL_AMOUNT;
    public static final ForgeConfigSpec.IntValue STIMULATED_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue OVERLOADED_DURATION_TICKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Ventana de ejecucion y Glory Kills").push("execution");
        EXECUTION_HEALTH_THRESHOLD = builder
                .comment("Vida (en puntos, 2 = 1 corazon) a la que un enemigo entra en estado Executable.")
                .defineInRange("healthThreshold", 5.0D, 0.5D, 1000.0D);
        EXECUTION_WINDOW_TICKS = builder
                .comment("Duracion de la ventana de ejecucion en ticks (20 ticks = 1 segundo).")
                .defineInRange("windowTicks", 60, 10, 1200);
        EXECUTION_RANGE = builder
                .comment("Distancia maxima (en bloques) para iniciar una ejecucion.")
                .defineInRange("range", 2.0D, 0.5D, 8.0D);
        BLEED_OUT_DEATH_CHANCE = builder
                .comment("Probabilidad (0-1) de que el enemigo muera solo si nadie lo ejecuta a tiempo.")
                .defineInRange("bleedOutDeathChance", 0.5D, 0.0D, 1.0D);
        EXECUTION_DURATION_TICKS = builder
                .comment("Duracion total de la ejecucion en ticks (debe coincidir con la animacion).")
                .defineInRange("durationTicks", 20, 1, 200);
        EXECUTION_IMPACT_TICK = builder
                .comment("Tick de la ejecucion en el que cae el golpe letal (sonido, sangre, muerte).")
                .defineInRange("impactTick", 10, 0, 200);
        builder.pop();

        builder.comment("Recompensas").push("rewards");
        HEAL_AMOUNT = builder
                .comment("Vida que recupera el jugador por cada Glory Kill.")
                .defineInRange("healAmount", 6.0D, 0.0D, 100.0D);
        STIMULATED_DURATION_TICKS = builder
                .comment("Duracion del efecto Stimulated en ticks.")
                .defineInRange("stimulatedTicks", 300, 1, 72000);
        OVERLOADED_DURATION_TICKS = builder
                .comment("Duracion del efecto Overloaded (bajon de adrenalina) en ticks.")
                .defineInRange("overloadedTicks", 1200, 1, 72000);
        builder.pop();

        SPEC = builder.build();
    }

    private GloryKillsConfig() {
    }
}
