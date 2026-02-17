FROM python:3.11-alpine

RUN adduser -D payment
WORKDIR /home/payment

COPY requirements.txt .
RUN pip install --no-cache-dir --default-timeout=100 -r requirements.txt

COPY listener.py .

RUN chown -R payment:payment /home/payment
USER payment

CMD ["python", "listener.py"]
