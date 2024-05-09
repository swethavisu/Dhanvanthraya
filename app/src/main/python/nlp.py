import spacy

nlp = spacy.load("en_core_web_sm")


def segregate_symptoms(text):
    doc = nlp(text)

    symptoms = []
    current_symptom = []

    for token in doc:
        if token.ent_type_ == "SYMPTOM":
            current_symptom.append(token.text)
        else:
            if current_symptom:
                symptoms.append(" ".join(current_symptom))
                current_symptom = []

    return symptoms
