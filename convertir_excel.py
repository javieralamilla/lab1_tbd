import pandas as pd
import json

# Leer el archivo Excel
try:
    df = pd.read_excel('tabla_extendida_rm.xlsx')
    print("Columnas disponibles en el Excel:")
    print(df.columns.tolist())
    print("\nPrimeras 5 filas:")
    print(df.head())
    
    # Convertir a JSON y guardarlo
    df_clean = df.dropna()  # Eliminar filas con valores nulos
    
    # Crear el archivo JSON
    data = df_clean.to_dict('records')
    
    with open('frontend/src/data/datos_comunas.json', 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False, default=str)
    
    print(f"\n✅ Archivo JSON creado exitosamente con {len(data)} registros")
    print("Ubicación: frontend/src/data/datos_comunas.json")
    
except Exception as e:
    print(f"Error procesando el archivo: {e}")