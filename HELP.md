sudo dnf install java-17-amazon-corretto-devel -y

java -jar spyfall-0.0.1-SNAPSHOT.jar

ssh -i <pem_key_location> ec2-user@<public_ip_address>

cai tu dong run 

1. sudo nano /etc/systemd/system/spyfall.service
2. set data
[Unit]
Description=Spyfall Spring Boot Application
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/masoi
ExecStart=sudo java -jar /home/ec2-user/masoi/spyfall-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target

Ctrl + O (O là chữ O, không phải số 0)

Màn hình dưới cùng sẽ hiện:

File Name to Write: /etc/systemd/system/spyfall.service

Chỉ cần nhấn: Enter để xác nhận lưu.

Bước 2: Thoát nano Nhấn: Ctrl + X

Nap lai cau hinh: sudo systemctl daemon-reload
bat tu dong chay: sudo systemctl enable spyfall
kiem tra trang thai: sudo systemctl status spyfall