package com.example.engine

object SampleXmlPresets {

    val ALIGHT_MOTION_PRESET = """
<?xml version="1.0" encoding="utf-8"?>
<!-- Alight Motion Preset v4.5.2 -->
<!-- Watermark: Edited by @rahul_vfx_official -->
<!-- Do not remove credits or re-upload -->
<scene width="1080" height="1920" fps="60" duration="15000">
    <!-- Main Video Layer -->
    <layer id="layer_main_video" label="Background Video" type="media">
        <transform x="540" y="960" scale="1.0" rotation="0" />
        <effect id="shake_effect" intensity="25" frequency="4.5" />
        <effect id="motion_blur" samples="16" />
    </layer>

    <!-- Beat Shake Overlay -->
    <shape id="shape_beat_flash" label="Beat Flash" fill="#FFFFFF" blend="add">
        <keyframes property="opacity">
            <key time="0" value="0.8" />
            <key time="250" value="0.0" />
        </keyframes>
    </shape>

    <!-- Creator Watermark Layer (Text) -->
    <text id="txt_watermark" label="Creator Watermark" text="@rahul_vfx_official" font="Montserrat-Bold" size="24" color="#80FFFFFF">
        <transform x="900" y="1800" opacity="0.65" />
    </text>

    <!-- Overlay Logo Shape Watermark -->
    <shape id="shape_watermark_badge" label="Watermark Logo Badge" fill="#FF1E293B">
        <transform x="900" y="1750" width="120" height="40" />
    </shape>

    <!-- Social Channel Credit -->
    <text id="txt_social_credit" label="Instagram Link" text="Follow on IG: @rahul_vfx_official | t.me/rahulpresets" size="18" color="#FFFFFF">
        <transform x="540" y="1880" />
    </text>

    <!-- Cinematic CC Color Grading -->
    <effect id="color_grade_teal_orange">
        <shadows color="#005577" />
        <highlights color="#FF8833" />
        <contrast value="1.25" />
    </effect>
</scene>
""".trimIndent()

    val CAPCUT_PREMIERE_PRESET = """
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xmeml>
<xmeml version="4">
    <!-- Premiere Project Preset with Author Branding -->
    <!-- Project Created by: @mauryavideos - Subscribe on YouTube: youtube.com/@mauryavideos -->
    <sequence id="seq_reel_edit_01">
        <name>Viral Reel Edit Preset</name>
        <duration>720</duration>
        <rate>
            <timebase>30</timebase>
            <ntsc>FALSE</ntsc>
        </rate>
        <media>
            <video>
                <track>
                    <clipitem id="clip_bg_01">
                        <name>Background Scene</name>
                        <start>0</start>
                        <end>720</end>
                    </clipitem>
                </track>
                <track>
                    <!-- Watermark Title Layer -->
                    <clipitem id="clipitem_watermark">
                        <name>WATERMARK_OVERLAY_MAURYA</name>
                        <generator>
                            <text>Preset by @mauryavideos</text>
                            <opacity>70</opacity>
                        </generator>
                    </clipitem>
                </track>
                <track>
                    <!-- Channel Branding Overlay -->
                    <clipitem id="clipitem_brand_badge">
                        <name>Brand_Watermark_Logo</name>
                        <link>https://instagram.com/mauryavideos</link>
                    </clipitem>
                </track>
            </video>
        </media>
    </sequence>
</xmeml>
""".trimIndent()

    val SVG_WATERMARK_PRESET = """
<?xml version="1.0" encoding="UTF-8"?>
<!-- SVG Vector Graphics with Creator Watermark -->
<!-- Copyright (c) 2026 @artist_design_hub -->
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="100%" height="100%">
    <defs>
        <linearGradient id="primary_gradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#3B82F6" />
            <stop offset="100%" stop-color="#8B5CF6" />
        </linearGradient>
    </defs>
    <!-- Background Card -->
    <rect width="800" height="800" rx="32" fill="url(#primary_gradient)" />
    <circle cx="400" cy="360" r="160" fill="#FFFFFF" opacity="0.9" />

    <!-- Obtrusive Diagonal Watermark Layer -->
    <g id="layer_watermark_overlay" opacity="0.35">
        <text x="400" y="400" font-family="Arial" font-size="52" font-weight="bold" fill="#FF0000" text-anchor="middle" transform="rotate(-35 400 400)">
            WATERMARK - PREVIEW ONLY
        </text>
    </g>

    <!-- Bottom Creator Signature -->
    <text id="txt_creator_signature" x="400" y="740" font-size="18" fill="#FFFFFF" text-anchor="middle">
        Created by @artist_design_hub | Do Not Copy
    </text>
</svg>
""".trimIndent()
}
