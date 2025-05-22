## 📋 Descrição do Projeto

Sistema de remessas internacionais entre usuários **Pessoa Física (PF)** e **Pessoa Jurídica (PJ)** com conversão de moedas em tempo real usando a API do Banco Central do Brasil.

### 🚀 Principais Funcionalidades

| Funcionalidade                                                                               | Emoji |
|---------------------------------------------------------------------------------------------|-------|
| Criação de usuários PF/PJ com validação de documentos únicos                                 | 👥    |
| Conversão de moedas usando dados oficiais do BCB                                             | 💱    |
| Cache inteligente para cotações                                                              | 📉    |
| Sistema de limites diários por tipo de usuário (PF: R$10k/dia • PJ: R$50k/dia)               | ⚖️    |
| Transações atômicas com rollback automático                                                   | 🔒    |
| Monitoramento via Actuator e métricas customizadas                                            | 📊    |

---

## 🛠 Tecnologias Utilizadas

### **Backend**
![Java](https://img.shields.io/badge/Java-21-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-6DB33F?logo=springboot)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-3.4.5-6DB33F?logo=spring)
![Resilience4j](https://img.shields.io/badge/Resilience4j-2.1.0-0052CC?logo=resilience4j)

### **Banco de Dados & Cache**
![H2](https://img.shields.io/badge/H2-Database-005571?logo=h2)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis)

### **Testes & Documentação**
![JUnit5](https://img.shields.io/badge/JUnit5-5.9.3-25A162?logo=junit5)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?logo=swagger)

### **Infraestrutura**
![Docker](https://img.shields.io/badge/Docker-24.0.7-2496ED?logo=docker)
![Maven](https://img.shields.io/badge/Maven-3.9.5-C71A36?logo=apachemaven)

---

## ⚙️ Como Executar

### **Pré-requisitos**
- ☕ [Java 21+](https://www.oracle.com/java/)
- 🐍 [Maven 3.6+](https://maven.apache.org/)
- 🐳 [Docker](https://www.docker.com/) 

### **Com Docker 🐳**
```bash
docker-compose up --build -d
```

### **Com Docker 🖥️**
```bash
./mvnw spring-boot:run
```

Acessos após inicialização Ferramenta:
Swagger UI	http://localhost:8080/swagger-ui.html
H2 Console	http://localhost:8080/h2-console


## 🚀 Melhorias Futuras
🔐 Implementar autenticação JWT/OAuth2

⏳ Adicionar fila de processamento assíncrono (RabbitMQ/Kafka)

📈 Criar dashboard de métricas (Grafana/Prometheus)

💸 Adicionar suporte a múltiplas moedas (EUR, GBP, JPY)

🌐 Implementar API de histórico de transações



PS.: me desculpem pelos commits juntos e sem descrição, ontem e hoje eu corri bastante pra terminar a prova.