![image](https://user-images.githubusercontent.com/45943968/131246620-5c037814-7466-444b-8383-be691c8e0b77.png)

# 원데이클래스 예약

본 예제는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 예제입니다.
이는 클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트들을 통과하기 위한 예시 답안을 포함합니다.
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW

# Table of contents

- [원데이클래스 예약](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [ConfigMap 설정](#ConfigMap-설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
    - [Self healing](#Liveness-Probe)


# 서비스 시나리오

원데이클래스 예약 서비스 

기능적 요구사항
1. 작가가 원데이클래스를 생성할 수 있다.
2. 고객이 원데이클래스를 선택하고, 예약요청과 함께 결재가 진행된다. 
3. 결제가 완료되면, 작가에게 예약내역이 전달된다.
4. 작가는 예약을 최종 승인/거절할 수 있다.
5. 작가가 거절하면 예약이 취소된다.(결제도 취소)
6. 고객이 예약을 취소할 수 있다.(결제도 취소)
7. 고객은 예약 상태를 확인하고, 작가는 원데이클래스/예약 상태를 확인할 수 있다.

비기능적 요구사항
1. 트랜잭션
    1) 결제가 되지 않은 예약건은 아예 거래가 성립되지 않아야 한다  ( Sync 호출 )
2. 장애격리
    1) 원데이클래스 관리 기능이 수행되지 않더라도 예약 주문은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
    2) 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다  Circuit breaker, fallback
3. 성능
    1) 전체적인 원데이클래스에 대한 정보 및 예약 상태 등을 한번에 확인할 수 있다. CQRS


# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
![분석설계0](https://user-images.githubusercontent.com/27762942/130011063-35d4610a-540a-43c8-a3b3-195e8ac0b6d4.png)

## TO-BE 조직 (Vertically-Aligned)
![분석설계1_new](https://user-images.githubusercontent.com/27762942/130180713-99d7d8ae-5b11-423c-9a56-15d9ba873dd2.png)

## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/8NHeQIRBXBfYXX7GUi12aM35loE3/2c6e78b704e2c380bd0c3d75fcd2c64d


### 이벤트 도출
![image](https://user-images.githubusercontent.com/45943968/131248031-8b570cf4-3554-4846-bf42-093b7b11eadc.png)

### 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/45943968/131248043-87c769ae-dc04-4fda-97c5-f790ea435e18.png)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - PaymentRequested : 예약요청 시 결재가 바로 진행되어야 하므로, ReservationRequested 이벤트에 통합하여 처리가 필요함. 커맨드로 변경 필요. 

### 액터, 커맨드, 폴리시 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/45943968/131248786-23f6c7bf-789c-44d5-97c0-7a6bea0dbc37.png)

### 어그리게잇으로 묶기
![image](https://user-images.githubusercontent.com/45943968/131248732-10e62936-fb15-454d-9dca-5c04cf181bfc.png)

    - Reservation, Payment, Lesson 은 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 바운디드 컨텍스트로 묶기
![image](https://user-images.githubusercontent.com/45943968/131248844-3cbdfe68-9a2f-4031-a607-5bc9df0d83e2.png)

    - 도메인 서열 분리 
        - Core Domain: Reservation, Lesson - 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 app 의 경우 1주일 1회 미만, store 의 경우 1개월 1회 미만
        - Supporting Domain: ViewPage - 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        - General Domain: Payment : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음

### 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

![image](https://user-images.githubusercontent.com/45943968/131249257-d218ac14-65c7-4e43-b53d-0316de1b0941.png)

### 완성된 1차 모형

![image](https://user-images.githubusercontent.com/45943968/131249276-8ec973c1-c45f-42f8-ac13-5ff6766580a3.png)

    - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/45943968/131250283-2adb4254-0eb8-4d57-8bd3-7ab7383d4eda.png)

    - 작가가 원데이클래스를 생성할 수 있다. (OK)
    - 고객이 원데이클래스를 선택하고, 예약요청과 함께 결재가 진행된다. (OK)
    - 결제가 완료되면, 작가에게 예약내역이 전달된다. (OK)
    - 작가는 예약을 최종 승인/거절할 수 있다. (OK)
    - 작가가 거절하면 예약이 취소된다.(결제도 취소) (OK)
    - 고객이 예약을 취소할 수 있다.(결제도 취소) (OK)
    - 고객은 예약 상태를 확인하고, 작가는 원데이클래스/예약 상태를 확인할 수 있다. (OK)
    - 전체적인 원데이클래스에 대한 정보 및 예약 상태 등을 한번에 확인할 수 있다. (?)
    
### 모델 수정

![image](https://user-images.githubusercontent.com/45943968/131250796-ac43b39d-35cc-42cc-9d3e-929403d83738.png)
    
    - 수정된 모델은 모든 요구사항을 커버함.

### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/45943968/131250822-3103ce37-a6ea-46cd-9cb2-fc27d4229464.png)

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 고객 예약 시 결제처리 : 결제가 완료되지 않은 예약은 절대 받지 않는다는 정책에 따라, ACID 트랜잭션 적용. 예약요청시 결제처리에 대해서는 Request-Response 방식 처리
        - 결제 완료시 작가 연결 및 예약 처리 : Reservation(front) 에서 lesson 마이크로서비스로 주문요청이 전달되는 과정에 있어서, lesson 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
        - 나머지 모든 inter-microservice 트랜잭션 : 예약상태 등 모든 이벤트에 대해 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.


## 헥사고날 아키텍처 다이어그램 도출
    
![헥사고날_new](https://user-images.githubusercontent.com/27762942/130165884-187c7007-b1e7-4729-a47b-c2f8880f74ce.png)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
   cd reservation
   mvn spring-boot:run
   
   cd payment
   mvn spring-boot:run
   
   cd lesson
   mvn spring-boot:run
   
   cd viewpage
   mvn spring-boot:run
   
   cd gateway
   mvn spring-boot:run
   
```

## CQRS

원데이클래스 생성 및 예약/결재 등 총 Status 에 대하여 고객/작가가 조회 할 수 있도록 CQRS 로 구현하였다.
- reservation, payment, lesson 개별 Aggregate Status 를 통합 조회하여 성능 Issue 를 사전에 예방할 수 있다.
- 비동기식으로 처리되어 발행된 이벤트 기반 Kafka 를 통해 수신/처리 되어 별도 Table 에 관리한다
- Table 모델링

  ![image](https://user-images.githubusercontent.com/45943968/131672665-88204e0c-abae-4478-93d8-82ba3342f775.png)

- viewPage MSA PolicyHandler 를 통해 구현 
  ("LessonCreated" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)

  ![image](https://user-images.githubusercontent.com/45943968/131673000-dd144068-c598-49e5-b298-3587af83fa25.png)
  
  ("ReservationRequested" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)

  ![image](https://user-images.githubusercontent.com/45943968/131673063-56ad9df1-85a9-40e0-b832-b0a1af11abc4.png)

- 실제로 view 페이지를 조회해 보면 모든 lesson 에 대한 정보, 예약 상태, 결제 상태 등의 정보를 종합적으로 알 수 있다.

http http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/reservationViews

![image](https://user-images.githubusercontent.com/45943968/131789977-dd1820ca-2e37-450d-aad5-fb35dd4f343d.png)


  
## API 게이트웨이

      1. gateway 스프링부트 App을 추가 후 application.yaml내에 각 마이크로 서비스의 routes 를 추가하고 gateway 서버의 포트를 8080 으로 설정함
      
          - application.yaml 예시
            ```
               spring:
		  profiles: docker
		  cloud:
		    gateway:
		      routes:
			- id: reservation
			  uri: http://user03-reservation:8080
			  predicates:
			    - Path=/reservations/** 
			- id: payment
			  uri: http://user03-payment:8080
			  predicates:
			    - Path=/payments/** 
			- id: lesson
			  uri: http://user03-lesson:8080
			  predicates:
			    - Path=/lessons/** 
			- id: viewpage
			  uri: http://user03-viewpage:8080
			  predicates:
			    - Path= /reservationViews/**
		      globalcors:
			corsConfigurations:
			  '[/**]':
			    allowedOrigins:
			      - "*"
			    allowedMethods:
			      - "*"
			    allowedHeaders:
			      - "*"
			    allowCredentials: true

		server:
		  port: 8080
            ```

         
      2. deployment.yaml 설정 내용 
          
            ```
		apiVersion: apps/v1
		kind: Deployment
		metadata:
		  name: user03-gateway
		  namespace: onedayclass
		  labels:
		    app: user03-gateway
		spec:
		  replicas: 1
		  selector:
		    matchLabels:
		      app: user03-gateway
		  template:
		    metadata:
		      labels:
			app: user03-gateway
		    spec:
		      containers:
		      - name: user03-gateway
			image: 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-gateway:v1
			ports:
			- containerPort: 8080

            ```               

      3. service.yaml 파일에 Service 설정 내용
          
            ```
                apiVersion: v1
		kind: Service
		metadata:
		  name: user03-gateway
		  namespace: onedayclass
		  labels:
		    app: user03-gateway
		spec:
		  ports:
		    - port: 8080
		      targetPort: 8080
		  selector:
		    app: user03-gateway
		    type:
		      LoadBalancer 
            ```             
 
depolyment.yaml 및 service.yaml 적용 후, Deploy, Service 및 API Gateway 엔드포인트 확인

![image](https://user-images.githubusercontent.com/45943968/131781815-50631a77-2eae-4c1c-9217-be6e2e2ffd2c.png)



# Correlation

해당 프로젝트에서는 PolicyHandler에서 처리 시 어떤 건에 대한 처리인지를 구별하기 위한 Correlation-key 구현을 
이벤트 클래스 안의 변수로 전달받아 서비스간 연관된 처리를 정확하게 구현하고 있습니다. 

아래의 구현 예제를 보면,
예약(Reservation)을 하면 동시에 연관된 수업(lesson), 결제(Payment) 등의 서비스의 상태가 적당하게 변경이 되고,
예약건의 취소를 수행하면 다시 연관된 수업(lesson), 결제(Payment) 등의 서비스의 상태값 등의 데이터가 적당한 상태로 변경되는 것을 확인할 수 있습니다.


예약 등록
http POST http http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/reservations customerId=1 customerName=“soyeon” authorId=1 authorName="jon" lessonId=1 lessonName="Cook" lessonPrice=100 lessonDate=2021-09-01 reservationStatus=“RSV_REQUESTED" paymentStatus="PAY_REQUESTED"

![image](https://user-images.githubusercontent.com/45943968/131788047-e0838c96-0544-40e1-9df2-e36a95637adf.png)

예약 후 - 예약 상태
http http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/reservations/1

![image](https://user-images.githubusercontent.com/45943968/131788158-7fb788cf-7592-4a30-aa57-d09d36e7e7ea.png)

예약 후 - 결제 상태
http http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/payments/1

![image](https://user-images.githubusercontent.com/45943968/131788253-f5afb443-19fa-4088-b2fc-010b8789e227.png)

예약 취소
http PATCH http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/reservations/1 reservationStatus="RSV_CANCELED"

![image](https://user-images.githubusercontent.com/45943968/131788418-027f2fe8-ee50-44be-908b-c54980f60834.png)

취소 후 - 예약 상태
http http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/reservations/1

![image](https://user-images.githubusercontent.com/45943968/131788467-bd05649a-7a99-4b2f-8c4a-629b4a3abd91.png)

취소 후 - 결제 상태
http http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/payments/1

![image](https://user-images.githubusercontent.com/45943968/131788524-aa59cd1b-31ff-48a6-a3bc-7b6e2619e3aa.png)



## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시는 Reservation 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 현실에서 발생가는한 이벤트에 의하여 마이크로 서비스들이 상호 작용하기 좋은 모델링으로 구현을 하였다.

```
package onedayclass;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;  		  	//예약 ID
    private Long customerId;	  	//고객 ID
    private String customerName;  	//고객명
    private Long authorId;	  	//작가 ID
    private String authorName;	  	//작가명
    private Long lessonId;		//수업 ID
    private String lessonName;		//수업명
    private Date lessonDate;		//수업 일자
    private Long lessonPrice;		//수업 가격
    private String reservationStatus;	//예약 상태 ("RSV_REQUESTED", "RSV_APPROVED", "RSV_CANCELED", "RSV_REJECTED", "LESSON_CREATED")
    private String paymentStatus;	//결재 상태 ("PAY_REQUESTED", "PAY_FINISHED", "PAY_CANCELED")
    
    -- 생략 --
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public Long getAuthorId() {
        return authorId;
    }
    
    -- 생략 --
}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package onedayclass;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reservations", path="reservations")
public interface ReservationRepository extends CrudRepository<Reservation, Long>{

}

```
- 적용 후 REST API 의 테스트
```

# reservation 서비스의 예약 요청
http POST http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/reservations customerId=1 customerName=“soyeon” authorId=1 authorName="jon" lessonId=1 lessonName="Cook" lessonPrice=100 lessonDate=2021-09-01 reservationStatus=“RSV_REQUESTED" paymentStatus="PAY_REQUESTED"

# reservation 서비스의 예약 상태 확인
http http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/reservations

```


## 동기식 호출(Sync) 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(reservation)->결제(payment) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient로 이용하여 호출하도록 한다.

- 결제 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# PaymentService.java

package onedayclass.external;

-- import문 생략 --

@FeignClient(name="payment", url="${prop.pay.url}")
public interface PaymentService {
    @RequestMapping(method= RequestMethod.POST, path="/payments/requestPayment")
    public boolean requestPayment(@RequestBody Payment payment);

}
```

- 예약 요청을 받은 직후(@PostPersist) 가능상태 확인 및 결제를 동기(Sync)로 요청하도록 처리
```
# Reservation.java (Entity)

    @PostPersist
    public void onPostPersist(){
        //원데이클래스 예약 요청
        onedayclass.external.Payment payment = new onedayclass.external.Payment();
        payment.setReservationId(this.getId());
        payment.setCustomerId(this.getCustomerId());
        payment.setCustomerName(this.getCustomerName());
        payment.setAuthorId(this.getAuthorId());
        payment.setAuthorName(this.getAuthorName());
        payment.setLessonId(this.getLessonId());
        payment.setLessonName(this.getLessonName());
        payment.setLessonPrice(this.getLessonPrice());
        payment.setPaymentStatus("PAY_FINISHED");

        Boolean result = ReservationApplication.applicationContext.getBean(onedayclass.external.PaymentService.class)
            .requestPayment(payment);

        if(result){
            ReservationRequested reservationRequested = new ReservationRequested();
            BeanUtils.copyProperties(this, reservationRequested);
            reservationRequested.publishAfterCommit();
        }
    }
    
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인

```
# 결제 (payment) 서비스를 잠시 내려놓음 (ctrl+c)

# 예약 요청  - Fail
http POST http://localhost:8088/reservations customerId=1 customerName=“soyeon” authorId=1 authorName="jon" lessonId=1 lessonName="Cook" lessonPrice=100 lessonDate=2021-09-01 reservationStatus=“RSV_REQUESTED" paymentStatus="PAY_REQUESTED"

# 결제서비스 재기동
cd payment
mvn spring-boot:run

# 예약 요청  - Success
http POST http://localhost:8088/reservations customerId=1 customerName=“soyeon” authorId=1 authorName="jon" lessonId=1 lessonName="Cook" lessonPrice=100 lessonDate=2021-09-01 reservationStatus=“RSV_REQUESTED" paymentStatus="PAY_REQUESTED"

```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커 처리는 운영단계에서 설명한다.)



## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

결제가 이루어진 후에 Reservation 서비스의 상태가 업데이트 되고, lesson 서비스의 상태 업데이트가 비동기식으로 호출된다.
- 이를 위하여 결제가 승인되면 결제가 승인 되었다는 이벤트를 카프카로 송출한다. (Publish)
 
```
# Payment.java

package onedayclass;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Payment_table")
public class Payment {

    ....
    @PostPersist
    public void onPostPersist(){
        //결재 승인 처리 
        PaymentFinished paymentFinished = new PaymentFinished();
        BeanUtils.copyProperties(this, paymentFinished);
        paymentFinished.publishAfterCommit();
    }
    ....
}
```

- 예약 시스템에서는 결제 승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
# PolicyHandler.java

package onedayclass;

@Service
public class PolicyHandler{
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentFinished_UpdateReservation(@Payload PaymentFinished paymentFinished){

        if(!paymentFinished.validate()) return;

        System.out.println("\n\n##### listener paymentFinished : " + paymentFinished.toJson() + "\n\n");
        // view 객체 조회
        Optional<Reservation> res = reservationRepository.findById(paymentFinished.getReservationId());
        Reservation reservation = res.get();

        if(reservation != null){
            reservation.setPaymentStatus("PAY_FINISHED");
            // view 레파지 토리에 update
            reservationRepository.save(reservation);
        }

    }
```

그 외 예약 승인/거부는 예약/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 유지보수로 인해 잠시 내려간 상태 라도 예약을 받는데 문제가 없다.

```
# 수업(lesson) 서비스를 잠시 내려놓음 (ctrl+c)

# 예약 요청  - Success
http POST http://localhost:8088/reservations customerId=1 customerName=“soyeon” authorId=1 authorName="jon" lessonId=1 lessonName="Cook" lessonPrice=100 lessonDate=2021-09-01 reservationStatus=“RSV_REQUESTED" paymentStatus="PAY_REQUESTED"

# 예약 상태 확인  - lesson 서비스와 상관없이 예약 상태는 정상 확인
http GET http://localhost:8088/reservations
```



## 폴리글랏 퍼시스턴스

viewPage 는 RDB 계열의 데이터베이스인 Maria DB 를 사용하기로 하였다. 
별다른 작업없이 기존의 Entity Pattern 과 Repository Pattern 적용과 데이터베이스 관련 설정 (pom.xml, application.yml) 만으로 Maria DB 에 부착시켰다.

```
# ReservationView.java

package onedayclass;

import javax.persistence.*;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="ReservationView_table")
public class ReservationView {

}

# ReservationViewRepository.java

package onedayclass;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ReservationViewRepository extends CrudRepository<ReservationView, Long> {
    ReservationView findByLessonId(Long lessonId);
    ReservationView findByReservationId(Long reservationId);
}

# pom.xml

	<dependency>
		<groupId>org.mariadb.jdbc</groupId>
		<artifactId>mariadb-java-client</artifactId>
		<scope>runtime</scope>
	</dependency>
		

# application.yml

  datasource:
    url: jdbc:mariadb://localhost:3306/test
    driver-class-name: org.mariadb.jdbc.Driver
    username: ####   (계정정보 숨김처리)
    password: ####   (계정정보 숨김처리)

```

실제 MariaDB 접속하여 확인 시, 데이터 확인 가능 (ex. reservation에서 예약 요청한 경우)

![image](https://user-images.githubusercontent.com/45943968/131681312-d9009957-10f9-4523-bf4f-ff27c9509d2b.png)



# 운영

## CI/CD 설정

각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS를 사용하였다.
docker 명령어를 통해 이미지를 생성하고, 프로젝트별 deployment.yaml, service.yaml 파일을 작성하여 배포하였다. 


AWS ECR 현황

![image](https://user-images.githubusercontent.com/45943968/131780296-e45d69cc-d85d-40c6-9534-1394d04acb08.png)

AWS ECR에 이미지 생성

```
1. 각 서비스 별 폴더에서 mvn package 실행 -> docker 명령어를 통해 이미지 build 및 push

cd reservation
mvn package
docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-reservation:v1 .
docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-reservation:v1

cd payment
mvn package
docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-payment:v1 .
docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-payment:v1

cd lesson
mvn package
docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-lesson:v1 .
docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-lesson:v1

cd viewpage
mvn package
docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-viewpage:v1 .
docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-viewpage:v1

cd gateway
mvn package
docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-gateway:v1 .
docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-gateway:v1

```

EKS에 배포 확인

```
각 서비스별 하위 폴더인 kubemetes 의 depolyment.yaml, service.yaml 파일 배포
( reservation, payment, lesson, viewpage, gateway 모두 진행 )

kubectl apply -f deployment.yml
kubectl apply -f service.yaml

```

![image](https://user-images.githubusercontent.com/45943968/131787052-75315b74-2aba-4107-865f-e18442f29164.png)



## ConfigMap 설정

 예약(reservation) -> 결제(payment) 로의 동기 호출 URL을 ConfigMap에 등록하여 사용하였다.

 kubectl apply -f configmap.yaml
 
```
apiVersion: v1
 kind: ConfigMap
 metadata:
   name: reservation-configmap
   namespace: onedayclass
 data:
   payurl: "http://user03-gateway:8080"

```

예약(reservation) 서비스의 deployment.yaml 수정

```
            spec:
	      containers:
	      - name: user03-reservation
		image: 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-reservation:v1
		ports:
		- containerPort: 8080
		env:
		- name: payurl
		  valueFrom:
		    configMapKeyRef:
		      name: reservation-configmap
		      key: payurl 
                        
```            
application.yml 수정
```
prop:
  pay:
    url: ${payurl}
``` 

동기 호출 URL 실행 - 예약 요청

http POST http http://a0e469d08505d48aa90f700f1205288c-1141681070.ap-northeast-2.elb.amazonaws.com:8080/reservations customerId=1 customerName=“soyeon” authorId=1 authorName="jon" lessonId=1 lessonName="Cook" lessonPrice=100 lessonDate=2021-09-01 reservationStatus=“RSV_REQUESTED" paymentStatus="PAY_REQUESTED"

![image](https://user-images.githubusercontent.com/45943968/131788047-e0838c96-0544-40e1-9df2-e36a95637adf.png)



## 동기식 호출 / 서킷 브레이킹 / 장애격리


시나리오는 예약(reservation)-->결제(payment) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 
결제 요청이 과도할 경우 CB 를 통하여 장애격리.

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인: 동시사용자 10명, 10초 동안 실시

```
# siege 서비스 생성
kubectl run siege --image=apexacme/siege-nginx -n onedayclass

# seige pod 접속
kubectl exec -it pod/siege-d484db9c-42d8q -c siege -n onedayclass -- /bin/bash

# UR 호출
siege -c10 -t10s -v -content-type "application/json" 'http://user03-reservation:8080/reservations POST {"customerId":1,"customerName":"soyeon","authorId":1,"authorName":"jon","lessonId":1,"lessonName":"Cook","lessonPrice":100,"lessonDate":2021-09-01,"reservationStatus":"RSV_REQUESTED","paymentStatus":"PAY_REQUESTED"}'
```

* CB가 없기 때문에 100% 성공

![image](https://user-images.githubusercontent.com/45943968/131796548-a7e97588-a294-400e-a8a1-568429c4ae8c.png)

* 서킷 브레이킹 프레임워크의 선택: istio의 VirtualService 적용

```
# istio-injection 활성화
kubectl label namespace onedayclass istio-injection=enabled 

# VirtualService 적용 
kubectl apply -f VirtualService.yaml

apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: vs-rev
  namespace: onedayclass
spec:
  hosts:
  - "user03-reservation"
  http:
  - route:
    - destination:
        host: "user03-reservation"
    timeout: 0.05s
	
```

* CB적용 되어 일부 실패 확인

![image](https://user-images.githubusercontent.com/45943968/131803655-22733ecf-3c95-4ec1-ae34-362369d5aca0.png)



## 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에,
이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

- 결제(payment) 서비스에 대한 pod를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 10프로를 넘어서면 pod를 10개까지 늘려준다.
```
kubectl autoscale deployment user03-payment --cpu-percent=10 --min=1 --max=10 -n onedayclass
```

![image](https://user-images.githubusercontent.com/45943968/131807140-87dbc107-a80c-434c-be9d-1c2704f0af8f.png)

- CB 에서 했던 방식대로 부하 발생
```
siege -c10 -t10s -v  http://user03-gateway:8080/payments
```

- 서비스 사용률 증가
![image](https://user-images.githubusercontent.com/45943968/131808012-822fafa8-e666-4b98-b647-56872ed1243f.png)

- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다. 

watch kubectl get pod -n onedayclass

![image](https://user-images.githubusercontent.com/45943968/131807809-60015f03-fe17-4b7d-9fed-ba67e3171fae.png)

![image](https://user-images.githubusercontent.com/45943968/131809985-99ef2929-50dc-4efa-9734-4154fbdfaa55.png)



## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
siege -c100 -t10S -v --content-type "application/json" 'http://user04-customer:8080/reservations'

```

```
# buildspec.yaml 의 readiness probe 의 설정:

                    readinessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 10
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 10
```

Customer 서비스 신규 버전으로 배포

![readiness](https://user-images.githubusercontent.com/87056402/130174091-65759533-049d-4fca-aeca-3c2a52d61925.png)

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.

## Liveness Probe

테스트를 위해 deployment.yml을 아래와 같이 수정 후 배포

```
	      containers:
	      - name: user03-viewpage
		image: 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user03-viewpage:v1
		ports:
		- containerPort: 8080
		resources:
		  requests:
		    cpu: "250m"
		  limits:
		    cpu: "500m"
		args:
		- /bin/sh
		- -c
		- touch /tmp/healthy; sleep 30; rm -rf /tmp/healthy; sleep 600
		livenessProbe:
		  exec:
		    command:
		    - cat
		    - /tmp/healthy
		  initialDelaySeconds: 120
		  timeoutSeconds: 2
		  periodSeconds: 5
		  failureThreshold: 5
```

컨테이너 실행 후 30초 동인은 정상이나, 이후 /tmp/healthy 파일이 삭제되어 livenessProbe에서 실패를 리턴하게 됨. (이후 자동으로 재시작)
```
kubectl describe pod/user03-viewpage-7589d5dfbc-z6qhm -n onedayclass 
```

![image](https://user-images.githubusercontent.com/45943968/131827853-27c2e7f4-8bb4-4f4c-86f7-ed1620f6a6c6.png)

pod의 restart 횟수가 증가함을 확인

![image](https://user-images.githubusercontent.com/45943968/131828212-08ad6035-32da-49b8-abe5-d291535db136.png)

