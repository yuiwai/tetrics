# tetrics

4方向版テトリス実装です。

## Demo 

https://lab.yuiwai.com/tetrics/

## How to play

Move Right: L  
Move Left: H  
Move Up: K  
Move Down: J  
Drop Right: Shift + L  
Drop Left: Shift + H  
Drop Top: Shift + K  
Drop Bottom: Shift + J  
Rotate Right: F  
Rotate Left: D  

## 思考部分の外部化

思考部分を自作して結合することも可能です。

### クライアント

下記URLから、libGDX版実装のjarファイルをダウンロードしてください  
http://lab.yuiwai.com/tetrics/download/tetrics-libgdx.jar

以下のコマンドで起動できます  
```$ java -jar tetrics-libgdx.jar```

デフォルトでは、 `http://localhost:8080` に接続します。

接続先を変更するには起動時にパラメータで指定します。  
```$ java -Dconnector.url=http://example.com/ -jar tetrics-libgdx.jar```

### サーバ

サーバ側の実装については下記の定義を参照してください。  
https://github.com/yuiwai/tetrics.proto

